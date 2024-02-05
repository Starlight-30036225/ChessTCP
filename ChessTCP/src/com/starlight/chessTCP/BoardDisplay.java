package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BoardDisplay {

    public static JFrame frame;
    public static SimplePiece[][] Board;
    static Image[] imgList;

    static List<Float> possibleMoves;

    static SimplePiece SelectedPiece;

    static int MouseX, MouseY;
    public BoardDisplay() {
        LoadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
    }

    public static void printMap() {

        ExtractImages();

        frame = getFrame();

        PaintFrame(frame);

        //SetUpMouseListeners(frame);

        frame.setVisible(true);
    }


    private static void ExtractImages() {
        BufferedImage BaseImage;

        try {
            BaseImage = ImageIO.read(new File("src/Resources/chess.png"));
        } catch (IOException e) {
            System.out.println("Couldn't find image");
            throw new RuntimeException(e);
        }

        imgList = new Image[12];        //Creates an array of 12 images

        int ind = 0;        //saves the index

        //stolen code
        for (int y = 0; y < 400; y += 200) {
            for (int x = 0; x < 1200; x += 200) {
                imgList[ind] = BaseImage.getSubimage(x, y, 200, 200).getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH);
                ind++;
            }
        }
    }

    private static JFrame getFrame() {

        //sets up JFrame
        JFrame frame = new JFrame();
        frame.setBounds(200, 100, 528, 600);
        frame.setUndecorated(false);
        frame.setName("CHESS");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setBackground(Color.LIGHT_GRAY);
        return frame;
    }
    private static void PaintFrame(JFrame frame) {
        JPanel pn = new JPanel() {
            @Override
            public void paint(Graphics g) {
                boolean white = true;       //FlipFlops
                for (int Y = 0; Y < 8; Y++) {       //8 rows and 8 cols
                    for (int X = 0; X < 8; X++) {
                        if (white) {
                            g.setColor(Color.YELLOW);

                        } else {
                            g.setColor(Color.PINK);
                        }

                        white = !white;  //flips the flop

                        g.fillRect(X * 64, Y * 64, 64, 64);
                    }
                    white = !white;
                }
                DrawPieces(g);

                //Draws the info bar at the bottom

                g.setColor(Color.BLACK);
                //g.drawString("White: " + Board.getWhiteState(), 140, 540);
                //g.drawString("Black: " + Board.getBlackState(), 300, 540);
                //g.drawString((Board.whitesTurn ? "White's": "Black's") + " turn.", 40, 540);
                g.setColor(Color.WHITE);

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
                            g.drawImage(imgList[imageVal], p.getX() * 64, p.getY() * 64, this);
                        }

                    }
                }
            }


        };
        frame.add(pn);
    }

    private void LoadMapFromNotation(String Notation) {
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

        char rank = (char) (x + 97);

        return rank + Integer.toString(y);
    }
}
