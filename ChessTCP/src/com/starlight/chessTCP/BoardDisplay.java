package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Font.*;

public class BoardDisplay {

    public boolean White;

    public  JFrame frame;
    public  SimplePiece[][] Board;
    Image[] imgList;
    public  List<String> possibleMoves;

    SimplePiece SelectedPiece;

    int MouseX, MouseY;

    public final int MARGIN = 32;
    public final int BOARD_WIDTH = 528;
    public final int BOARD_HEIGHT = 600;

    public final int SQUARE_SIZE = 64;
    public BoardDisplay(boolean white) {
        this.White = white;
        possibleMoves = new ArrayList<>();
        //LoadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");


    }

    public void printMap() {

        ExtractImages();

        frame = getFrame();

        PaintFrame(frame);

        //SetUpMouseListeners(frame);

        frame.setVisible(true);
    }

    private void ExtractImages() {
        BufferedImage BaseImage;

        //gets sprite sheet
        try {
            BaseImage = ImageIO.read(new File("src/Resources/chess2.png"));
        } catch (IOException e) {
            System.out.println("Couldn't find image");
            throw new RuntimeException(e);
        }

        imgList = new Image[12];        //Creates an array of 12 images
        int width = BaseImage.getWidth();
        int height = BaseImage.getHeight();

        int ind = 0;        //saves the index

        for (int y = 0; y < height; y += height/2) {
            for (int x = 0; x < width; x += width /6) {
                imgList[ind] = BaseImage.getSubimage(x, y, width/6, height/2).getScaledInstance(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.SCALE_SMOOTH);
                ind++;
            }
        }
    }

    private JFrame getFrame() {

        //sets up JFrame
        JFrame frame = new JFrame();
        frame.setBounds(200, 100, BOARD_WIDTH + MARGIN, BOARD_HEIGHT + MARGIN);
        frame.setUndecorated(false);
        frame.setName("CHESS");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setBackground(Color.LIGHT_GRAY);
        return frame;
    }
    private void PaintFrame(JFrame frame) {
        JPanel pn = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.setFont(new Font("Serif", BOLD, 14));
                String rank;
                String file;
                boolean white = true;       //FlipFlops
                for (int Y = 0; Y < 8; Y++) {       //8 rows and 8 cols

                    // add text to label
                    g.setColor(Color.BLACK);
                    rank = String.valueOf(!White? (Y + 1) : 8 - Y); //Numbers on side
                    file = String.valueOf(White? (char) ((Y ) + 97) : (char) ((7 - Y ) + 97));      //Letters across top

                    g.drawString(rank, MARGIN/2, (int) (Y * SQUARE_SIZE + (MARGIN * 2.3)));
                    g.drawString(file, (Y * SQUARE_SIZE + (MARGIN * 2)),MARGIN/2);

                    //Draws checkered pattern
                    for (int X = 0; X < 8; X++) {
                        if (white) {
                            g.setColor(Color.magenta);

                        } else {
                            g.setColor(Color.PINK);
                        }

                        white = !white;  //flips the flop

                        g.fillRect(X * SQUARE_SIZE + MARGIN, Y * SQUARE_SIZE + MARGIN, SQUARE_SIZE, SQUARE_SIZE);
                        Highlight(g,X,Y);
                    }
                    white = !white;
                }
                DrawPieces(g);
            }
            private void DrawPieces(Graphics g) {
                for (SimplePiece[] Row :
                        Board) {
                    for (SimplePiece p:
                            Row) {
                        if (p == null) {continue;}  //No piece at this location, skip
                        int imageVal = p.getImageval();     //get image from piece
                        if (p == SelectedPiece) {   //if piece is selected, render on mouse not at bass location
                            g.drawImage(imgList[imageVal], MouseX, MouseY, this);
                        }
                        else{   //renders at piece location
                            g.drawImage(imgList[imageVal], p.getX() * SQUARE_SIZE + MARGIN, p.getY() * SQUARE_SIZE+MARGIN, this);
                        }

                    }
                }
            }
        };
        frame.add(pn);
    }

    public void LoadMapFromNotation(String Notation) {
        Board = new SimplePiece[8][8];    //Initialises the Pieces map
        int x = 0;                      //Starts at 0,0 (top left)
        int y = 0;
        if (!this.White) {Notation = new StringBuilder(Notation).reverse().toString();}      //This will flip the notation to show black at the bottom


        for (Character c :               //Iterates through every character in the notation string
                Notation.toCharArray()) {

            if (x > 7) {
                x = 0;
                y++;
            }      //If the x value is more than 7, move to next line

            if (c == '/') {
                continue;
            }   //Denotes end of line

            if (Character.isDigit(c)) {
                x += Character.getNumericValue(c);
            } //Numbers denote empty space between pieces
            else {       //Otherwise, this slot should contain a piece
                boolean white;
                white = Character.isUpperCase(c);       //Capital is a white piece, lower case is black
                Board[x][y] = new SimplePiece(Character.toLowerCase(c), x, y, white);        //Uses the factory method inside Piece to create a Piece of the correct class
                x++;    //increments X (moving right)
            }

        }
    }

    private void Highlight(Graphics g, int X, int Y) {
        //gatekeeping
        if((possibleMoves == null || !possibleMoves.contains(X + "" + Y))){ return;} //This location is irrelvant, skip

        Color HighlightColor = (this.White == SelectedPiece.white)? new Color(0, 200, 0, 200) : new Color(200, 0, 0, 200) ;

        g.setColor(HighlightColor);

        //If there is a piece at the location, the circle needs to be a lil bigger
        int circleSize = Board[X][Y] == null? 24: 44;
        int offset = Board[X][Y] == null? 20: 10;
        g.fillOval(X * SQUARE_SIZE + MARGIN + offset, Y * SQUARE_SIZE + MARGIN + offset, circleSize, circleSize);
    }

    public String FlipCoords(String Coords){        //flips coords from white layout to black layout or vice versa

        int x = Character.getNumericValue(Coords.charAt(0)) + 1;
        int y = Character.getNumericValue(Coords.charAt(1)) + 1;
        x = (9 - x) ;
        y = (9 - y) ;
        x--;
        y--;

        return x + "" + y;
    }

    public void LoadPossibleMoves(List<String> newMoves){
        possibleMoves.clear();
        for (String s:
             newMoves) {
            possibleMoves.add(this.White? s : FlipCoords(s));
        }
    }
}

class SimplePiece{

    public SimplePiece(char pieceChar, int x ,int y, boolean white) {
        this.x = x;
        this.y = y;
        this.white = white;
        type = PieceEnum.valueOf(String.valueOf(pieceChar));

    }

    PieceEnum type;
    int x,y;
    boolean white;

    public int getImageval() {
        return type.BaseImageVal + (white? 0 : 6);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLocation(){

        return x + "" + y;
    }
}
