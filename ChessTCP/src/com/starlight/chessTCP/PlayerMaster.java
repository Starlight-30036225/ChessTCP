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

    public PlayerMaster() {
        pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999, this);
        pool.execute(client);
        Board = new BoardDisplay(true);
        Board.printMap();
        SetUpMouseListeners(Board.frame);
    }

    public void recieveBoardStatus(String Notation) {
        Board.LoadMapFromNotation(Notation);
        //Board.printMap();
        Board.frame.repaint();
        System.out.println(Notation);
    }

    public void receivePossibleMoves(List<String> possibleMoves) {
        Board.possibleMoves = possibleMoves;
        Board.frame.repaint();
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
                //Used for dragging

                //gets current mouse pos
                int MouseX = Board.MouseX = e.getX();
                int MouseY = Board.MouseY = e.getY();

                Board.MouseX -= 32;
                Board.MouseY -= 64; //tells the display where the mouse is, so it can display the selected piece over the mouse

                //Force Mouse to be in range
                if (MouseY > Board.BOARD_HEIGHT + Board.MARGIN) {MouseY = Board.BOARD_HEIGHT + Board.MARGIN;} else if (MouseY < 0) { MouseY =0;}
                if (MouseX > Board.BOARD_WIDTH + Board.MARGIN) {MouseX = Board.BOARD_WIDTH + Board.MARGIN;} else if (MouseX < 0) { MouseX =0;}

                //Convert mouse coords to board coords (plus 3 just seems to work)
                MouseX -= Board.MARGIN + 3;
                MouseY -= Board.MARGIN + 3;

                MouseX /= Board.SQUARE_SIZE + 3;
                MouseY /= Board.SQUARE_SIZE + 3;


                if (MouseX >= 8 || MouseY >= 8) {return;}   //gatekeeping if mouse is out of range of array
                Board.SelectedPiece = Board.Board[MouseX][MouseY];
                if (Board.SelectedPiece == null) {return;}      //not clicking a piece

                //Sends piece to server
                client.sendMessage(PacketHeader.SELECT_PIECE, Board.SelectedPiece.getLocation());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (Board.SelectedPiece == null) {return;}
                //Board.Move(Board.SelectedPiece.getX(),Board.SelectedPiece.getY(),(e.getX()/64),(e.getY()/64));
                client.sendMessage(PacketHeader.MOVE, Board.SelectedPiece.getLocation());
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
                Board.MouseY -= 64;

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


