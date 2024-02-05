package com.starlight.chessTCP;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMaster {

    public BoardDisplay Board;
    PlayerClient client;
    private ExecutorService pool;

    public PlayerMaster() {
        pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999);
        pool.execute(client);
        BoardDisplay b = new BoardDisplay();
        b.printMap();
        SetUpMouseListeners(b.frame);
    }

    public void SetUpMouseListeners(JFrame frame) {
        frame.addMouseListener(new MouseInputListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                //if (WaitingOnPromotion) {return;}
                //Used for dragging
                int MouseX = e.getX();
                int MouseY = e.getY();
                //Force Mouse to be in range
                if (MouseY > 512) {MouseY = 511;} else if (MouseY < 0) { MouseY =0;}
                if (MouseX > 512) {MouseX = 511;} else if (MouseX < 0) { MouseX =0;}
                //Convert mouse coords to board coords
                MouseX /= 64;
                MouseY /= 64;

                Board.SelectedPiece = Board.Board[MouseX][MouseY];
                if (Board.SelectedPiece == null) {return;}
                client.sendMessage(PacketHeader.SELECT_PIECE, Board.SelectedPiece.getLocation());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (Board.SelectedPiece == null) {return;}
                //Board.move(SelectedPiece.getX(),SelectedPiece.getY(),(e.getX()/64),(e.getY()/64));
                Board.SelectedPiece =null;
                Board.possibleMoves = null;
                Board.frame.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

        });
        frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (Board.SelectedPiece == null) {return;}
                Board.MouseX = e.getX();
                Board.MouseY = e.getY();
                if (Board.MouseY > 512) {Board.MouseY = 512;} else if (Board.MouseY < 0) {Board.MouseY =0;}
                if (Board.MouseX > 512) {Board.MouseX = 512;} else if (Board.MouseX < 0) {Board.MouseX =0;}

                Board.MouseX -= 32;
                Board.MouseY -= 32;

                frame.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }


    public static void main(String[] args) {
        PlayerMaster player = new PlayerMaster();
    }
}


