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
        Board = new BoardDisplay(false);
        Board.printMap();
        SetUpMouseListeners(Board.frame);
    }

    public void recieveBoardStatus(String Notation) {
        Board.LoadMapFromNotation(Notation);
        //Board.printMap();
        Board.frame.repaint();
        //System.out.println(Notation);
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
                if (!requestNewPossibles) { return;}
                //gets current mouse pos
                int MouseX = Board.MouseX = (e.getX() -Board.MARGIN); ;
                int MouseY = Board.MouseY = (e.getY()- (Board.MARGIN + 32));
                System.out.println(MouseX + "   " + MouseY);
                //MouseX -= Board.MARGIN;
                //MouseY -= Board.MARGIN + 32;
                //tells the display where the mouse is, so it can display the selected piece over the mouse

                //Force Mouse to be in range
                if (MouseY > Board.BOARD_HEIGHT) {MouseY = Board.BOARD_HEIGHT;} else if (MouseY < 0) { return;}//MouseY =0;}
                if (MouseX > Board.BOARD_WIDTH) {MouseX = Board.BOARD_WIDTH;} else if (MouseX < 0) { return;}//MouseX =0;}

                //Convert mouse coords to board coords (plus 3 just seems to work)
                //MouseX -= Board.SQUARE_SIZE;
                //MouseY -= Board.SQUARE_SIZE;

                MouseX /= Board.SQUARE_SIZE;
                MouseY /= Board.SQUARE_SIZE;


                if (MouseX >= 8 || MouseY >= 8) {return;}   //gatekeeping if mouse is out of range of array
                Board.SelectedPiece = Board.Board[MouseX][MouseY];
                if (Board.SelectedPiece == null) {return;}      //not clicking a piece

                //Sends piece to server
                String Location = Board.SelectedPiece.getLocation();
                if (!Board.White) {Location = Board.FlipCoords(Location);}

                    client.sendMessage(PacketHeader.SELECT_PIECE, Location);



                requestNewPossibles = false;
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
            public void mouseDragged(MouseEvent e) {
                if (Board.SelectedPiece == null) {return;}
                Board.MouseX = e.getX();
                Board.MouseY = e.getY();
                if (Board.MouseY > Board.BOARD_HEIGHT+ Board.MARGIN) {Board.MouseY = Board.BOARD_HEIGHT+ Board.MARGIN;}
                else if (Board.MouseY < Board.MARGIN) {Board.MouseY = Board.MARGIN;}


                if (Board.MouseX > Board.BOARD_HEIGHT+ Board.MARGIN) {Board.MouseX = Board.BOARD_HEIGHT+ Board.MARGIN;}
                else if (Board.MouseX < Board.MARGIN) {Board.MouseX = Board.MARGIN;}

                Board.MouseX -= Board.MARGIN;
                Board.MouseY -= (Board.MARGIN + 32);

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


