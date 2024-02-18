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
        Board = new BoardDisplay(true);
        Board.printMap();
        SetUpMouseListeners(Board.frame);
    }

    public void recieveBoardStatus(String Notation) {
        Board.LoadMapFromNotation(Notation);
        Board.frame.repaint();
    }

    public void receivePossibleMoves(List<String> possibleMoves) {
        Board.LoadPossibleMoves(possibleMoves);
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
                if (!requestNewPossibles) { return;}    //Haven't recieved the last request yet
                //gets current mouse pos (and sends to server)
                int MouseX = Board.MouseX = (e.getX() -Board.MARGIN); ;
                int MouseY = Board.MouseY = (e.getY()- (Board.MARGIN + 32));


                //if mouse is out of range, ignore
                if (MouseY > Board.BOARD_HEIGHT || MouseY < 0
                        || MouseX > Board.BOARD_WIDTH || MouseX < 0) { return;}


                MouseX /= Board.SQUARE_SIZE;
                MouseY /= Board.SQUARE_SIZE;

                if (MouseX >= 8 || MouseY >= 8) {return;}   //gatekeeping if mouse is out of range of array, but shoulnt happen

                Board.SelectedPiece = Board.Board[MouseX][MouseY];
                if (Board.SelectedPiece == null) {return;}      //not clicking a piece, exit here

                //Sends piece to server
                String Location = Board.SelectedPiece.getLocation();

                if (!Board.White) {Location = Board.FlipCoords(Location);}

                client.sendMessage(PacketHeader.SELECT_PIECE, Location);

                requestNewPossibles = false;        //Currently requesting possible moves, cant ask for more until this is returned.
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if (Board.SelectedPiece == null) {return;}      //gatekeeping


                //Gets mouse location as board coords
                int x = ((e.getX() - Board.MARGIN)/ (Board.SQUARE_SIZE));
                int y = ((e.getY() - (Board.MARGIN + 32))/ (Board.SQUARE_SIZE));
                String location = x + "" + y;


                if (Board.possibleMoves.contains(location)) {   //Checks requested move is possible
                    client.sendMessage(PacketHeader.MOVE, Board.SelectedPiece.getLocation() + "" + location);   //Sends selected piece and move in 1 string
                }

                //clear relevant data and redraw board
                Board.SelectedPiece =null;
                Board.possibleMoves.clear();
                Board.frame.repaint();
                requestNewPossibles = true;
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
            public void mouseDragged(MouseEvent e) {        //keeps the selected piece in range
                if (Board.SelectedPiece == null) {return;}
                Board.MouseX = e.getX();
                Board.MouseY = e.getY();
                //keep in range of Y
                if (Board.MouseY > Board.BOARD_HEIGHT+ Board.MARGIN) {Board.MouseY = Board.BOARD_HEIGHT+ Board.MARGIN;}
                else if (Board.MouseY < Board.MARGIN) {Board.MouseY = Board.MARGIN;}

                //Keep in range of X
                if (Board.MouseX > Board.BOARD_HEIGHT+ Board.MARGIN) {Board.MouseX = Board.BOARD_HEIGHT+ Board.MARGIN;}
                else if (Board.MouseX < Board.MARGIN) {Board.MouseX = Board.MARGIN;}

                //account for margin
                Board.MouseX -= Board.MARGIN;
                Board.MouseY -= (Board.MARGIN + 32);
                //redraw with new mouse location
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


