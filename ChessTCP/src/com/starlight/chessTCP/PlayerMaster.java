package com.starlight.chessTCP;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMaster {

    public BoardDisplay Board;
    PlayerClient client;
    private ExecutorService pool;
    boolean requestNewPossibles = true;
    String BoardState;

    boolean started = false;
    public PlayerMaster() {
        pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999, this);
        pool.execute(client);

        Board = new BoardDisplay(false, this);
        //Board.printMap();

    }

    public void recieveBoardStatus(String ColourAndNotation) {
        String Notation = ColourAndNotation.substring(5);
        String Colour = ColourAndNotation.substring(0,5);
        if (!started){
            Board.printMap();
            started = true;
        }

        Board.turn = (Board.White == (Objects.equals(Colour, "WHITE")));
        BoardState = Notation;
        Board.LoadMapFromNotation(Notation);
        Board.frame.repaint();

    }

    public void receivePossibleMoves(List<String> possibleMoves) {

        Board.LoadPossibleMoves(possibleMoves);
        Board.frame.repaint();
        requestNewPossibles = true;

    }

    public void requestPossibleMoves(String Location) {
        if (!requestNewPossibles) {return;}
        client.sendMessage(PacketHeader.SELECT_PIECE, Location);

    }

    public void receiveWelcomePack(String WelcomePack) {
        switch (WelcomePack.substring(0, 5)) {
            case "WHITE" -> {
                Board.White = true;
            }
            case "BLACK" -> {
                Board.White = false;

            }
            default -> {
                Board.White = true;
                Board.spectator = true;
            }

        }
    }

    public void requestMove(String PieceLocation, String MoveLocation){
        if (Board.spectator) {requestNewPossibles = true; return;}
        client.sendMessage(PacketHeader.MOVE, PieceLocation + MoveLocation);   //Sends selected piece and move in 1 string
        requestNewPossibles = true;
    }

    public void RecievePromotionPrompt(){
        Board.promotion = !Board.promotion;
    }
    public void SendPromotion(char piece){
        client.sendMessage(PacketHeader.PROMOTION, String.valueOf(piece));   //Sends selected piece and move in 1 string

    }

    public static void main(String[] args) {
        PlayerMaster player = new PlayerMaster();
    }
}


