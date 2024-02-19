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

    public PlayerMaster() {
        pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999, this);
        pool.execute(client);
        Board = new BoardDisplay(true, this);
        Board.printMap();

    }

    public void recieveBoardStatus(String Notation) {
        Board.LoadMapFromNotation(Notation);
        Board.frame.repaint();
    }

    public void receivePossibleMoves(List<String> possibleMoves) {
        Board.LoadPossibleMoves(possibleMoves);
        Board.frame.repaint();
    }

    public void requestPossibleMoves(String Location) {
        if (!requestNewPossibles) {return;}
        client.sendMessage(PacketHeader.SELECT_PIECE, Location);

    }

    public void requestMove(String PieceLocation, String MoveLocation){

        client.sendMessage(PacketHeader.MOVE, PieceLocation + MoveLocation);   //Sends selected piece and move in 1 string
        requestNewPossibles = true;
    }

    public static void main(String[] args) {
        PlayerMaster player = new PlayerMaster();
    }
}


