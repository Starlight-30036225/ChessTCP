package com.starlight.chessTCP;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMaster {

    public BoardDisplay Board;
    PlayerClient client;
    private ExecutorService pool;
    boolean requestNewPossibles = true;
    String BoardState;
    Long start;
    Long end;
    int attempts;
    long totaltime;

    public PlayerMaster() {
        pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999, this);
        pool.execute(client);

        Board = new BoardDisplay(false, this);
        //Board.printMap();

    }

    public void recieveBoardStatus(String Notation) {
        Board.turn = !Board.turn;
        BoardState = Notation;
        Board.LoadMapFromNotation(Notation);
        Board.frame.repaint();
        end = System.currentTimeMillis();
        System.out.printf("%d%n", (end - start));
    }

    public void receivePossibleMoves(List<String> possibleMoves) {
        attempts++;
        Board.LoadPossibleMoves(possibleMoves);
        Board.frame.repaint();
        requestNewPossibles = true;
        end = System.currentTimeMillis();
        totaltime += (end-start);
        System.out.println("PM: " + totaltime / attempts + " -  " + attempts);;

    }

    public void requestPossibleMoves(String Location) {
        if (!requestNewPossibles) {return;}
        start = System.currentTimeMillis();
        client.sendMessage(PacketHeader.SELECT_PIECE, Location);

    }

    public void receiveWelcomePack(String WelcomePack) {
        switch (WelcomePack.substring(0, 5)) {
            case "WHITE" -> {
                Board.White = true;
                Board.turn = false;
            }
            case "BLACK" -> {
                Board.White = false;
                Board.turn = true;
            }
            default -> {
            }
        }
        Board.printMap();

    }

    public void requestMove(String PieceLocation, String MoveLocation){
        start = System.currentTimeMillis();
        client.sendMessage(PacketHeader.MOVE, PieceLocation + MoveLocation);   //Sends selected piece and move in 1 string
        requestNewPossibles = true;
    }

    public static void main(String[] args) {
        PlayerMaster player = new PlayerMaster();
    }
}


