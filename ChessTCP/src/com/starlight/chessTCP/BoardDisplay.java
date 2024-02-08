package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    public static final int SQUARE_SIZE = 64;
    public BoardDisplay(boolean white) {
        LoadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        this.White = white;
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

        try {
            BaseImage = ImageIO.read(new File("src/Resources/chess.png"));
        } catch (IOException e) {
            System.out.println("Couldn't find image");
            throw new RuntimeException(e);
        }

        imgList = new Image[12];        //Creates an array of 12 images

        int ind = 0;        //saves the index

        for (int y = 0; y < 400; y += 200) {
            for (int x = 0; x < 1200; x += 200) {
                imgList[ind] = BaseImage.getSubimage(x, y, 200, 200).getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH);
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

                boolean white = true;       //FlipFlops
                for (int Y = 0; Y < 8; Y++) {       //8 rows and 8 cols

                    // add text to label
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(Y + 1), MARGIN/2, (int) (Y * 64 + (MARGIN * 2.3)));
                    g.drawString(String.valueOf((char) ((Y ) + 97)),  (int) (Y * 64 + (MARGIN * 2)),MARGIN/2);

                    for (int X = 0; X < 8; X++) {
                        if (white) {
                            g.setColor(Color.YELLOW);

                        } else {
                            g.setColor(Color.PINK);
                        }

                        white = !white;  //flips the flop

                        g.fillRect(X * 64 + MARGIN, Y * 64 + MARGIN, 64, 64);
                        Highlight(g,Y,X);
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
                        if (p == null) {continue;}
                        int imageVal = p.getImageval();
                        if (p == SelectedPiece) {
                            g.drawImage(imgList[imageVal], MouseX, MouseY, this);
                        }
                        else{
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

    private void Highlight(Graphics g, int Y, int X) {

        if(possibleMoves != null && possibleMoves.contains(X + "" + Y)){
            Color HighlightColor;

            HighlightColor = (this.White == SelectedPiece.white)? new Color(0, 200, 0, 99) : new Color(200, 0, 0, 99) ;
            g.setColor(HighlightColor);
            if (Board[X][Y] == null) {
                g.fillOval((X * SQUARE_SIZE + (MARGIN + 20)), (Y * SQUARE_SIZE + MARGIN + 20), 24, 24);
            }
            else{
                g.fillOval(X * SQUARE_SIZE + MARGIN + 10, Y * SQUARE_SIZE + MARGIN + 10, 44, 44);
            }
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
    boolean white = true;

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
