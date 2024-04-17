package com.starlight.chessTCP;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Font.*;

public class BoardDisplay {
    public boolean white;
    public boolean spectator;
    public boolean turn;
    public  JFrame frame;
    SimplePiece[][] board;
    Image[] imgList;
    final List<String> possibleMoves;
    final UIHandler master;
    SimplePiece selectedPiece;
    int mouseX, mouseY;
    boolean promotion;

    //constants
    final int MARGIN = 32;
    final int BOARD_WIDTH = 528;
    final int BOARD_HEIGHT = 600;
    final int SQUARE_SIZE = 64;

    Color boardColour1;
    Color boardColour2;

    String spriteFile;


    public BoardDisplay(boolean white, UIHandler master) {
        this.white = white;
        possibleMoves = new ArrayList<>();
        board = new SimplePiece[8][8];
        this.master = master;
        promotion = false;

    }

    public void recievePlayerPreferences(int colourSelection, int spriteSelection) {
        spriteFile = switch (spriteSelection) {
            case 1 -> "chess.png";
            case 2-> "chess3.png";
            case 3-> "chess4.png";
            case 4-> "chess5.png";
            default -> "chess2.png";
        };

        switch (colourSelection) {
            case 1 -> { // Pink
                boardColour1 = Color.PINK;
                boardColour2 = Color.magenta;
            }
            case 2 -> { // Evil
                boardColour1 = Color.BLACK;
                boardColour2 = Color.RED;
            }
            case 3 -> { // Ugly
                boardColour1 = Color.GREEN;
                boardColour2 = Color.YELLOW;
            }
            case 4 -> { // New
                boardColour1 = Color.BLUE;
                boardColour2 = Color.ORANGE;
            }
            default -> {
                boardColour1 = Color.BLACK;
                boardColour2 = Color.WHITE;
            }
        }
    }

    public void printMap() {

        extractImages();

        frame = getFrame();

        paintFrame(frame);

        //SetUpMouseListeners(frame);
        frame.setVisible(true);
        setUpMouseListeners(frame);
    }

    private void extractImages() {
        BufferedImage baseImage;
        //gets sprite sheet
        try {
            baseImage = ImageIO.read(new File("src/Resources/" + spriteFile));
        } catch (IOException e) {
            System.out.println("Couldn't find image");
            throw new RuntimeException(e);
        }

        imgList = new Image[12];        //Creates an array of 12 images
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        int i = 0;        //saves the index

        for (int y = 0; y < height; y += height/2) {
            for (int x = 0; x < width; x += width /6) {
                imgList[i] = baseImage.getSubimage(x, y, width/6, height/2).getScaledInstance(SQUARE_SIZE, SQUARE_SIZE, BufferedImage.SCALE_SMOOTH);
                i++;
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
    private void paintFrame(JFrame frame) {
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
                    rank = String.valueOf(!BoardDisplay.this.white ? (Y + 1) : 8 - Y); //Numbers on side
                    file = String.valueOf(BoardDisplay.this.white ? (char) ((Y ) + 97) : (char) ((7 - Y ) + 97));      //Letters across top

                    g.drawString(rank, MARGIN/2, (int) (Y * SQUARE_SIZE + (MARGIN * 2.3)));
                    g.drawString(file, (Y * SQUARE_SIZE + (MARGIN * 2)),MARGIN/2);

                    //Draws checkered pattern
                    for (int X = 0; X < 8; X++) {
                        if (white) {
                            g.setColor(boardColour1);

                        } else {
                            g.setColor(boardColour2);
                        }

                        white = !white;  //flips the flop

                        g.fillRect(X * SQUARE_SIZE + MARGIN, Y * SQUARE_SIZE + MARGIN, SQUARE_SIZE, SQUARE_SIZE);
                        highlight(g,X,Y);
                        drawPiece(g,X,Y);
                    }
                    white = !white;
                }



                g.setColor(Color.BLACK);
                g.setFont(new Font("serif", PLAIN, spectator? 20 : turn? 20: 15));
                g.drawString(spectator? turn? "White's turn" : "Black's turn" :
                        turn? "Your turn" : "Opponent's turn", MARGIN, BOARD_HEIGHT - MARGIN);
                if (promotion) {
                    drawPromotionPanel(g); return;}
                if (selectedPiece == null) {return;}
                g.drawImage(imgList[selectedPiece.getImageVal()], mouseX, mouseY, this);

            }
            private void drawPiece(Graphics g, int x, int y) {
                SimplePiece p = board[x][y];
                if (p == null || p == selectedPiece) {return;}  //No piece at this location, skip
                int imageVal = p.getImageVal();     //get image from piece
                g.drawImage(imgList[imageVal], p.getX() * SQUARE_SIZE + MARGIN, p.getY() * SQUARE_SIZE+MARGIN, this);

            }

            private void drawPromotionPanel(Graphics g) {

                g.setColor(Color.CYAN);
                g.fillRect(0,0,64,256);
                g.setColor(Color.BLUE);
                g.drawRect(0,0,64,256);

                int color = white ? 1 : 7;
                for (int i = 0; i < 4; i++) {
                    g.drawImage(imgList[(color + i)], 0, i * 64, this);
                }

            }


        };
        frame.add(pn);
    }

    public void loadMapFromNotation(String notation) {
        board = new SimplePiece[8][8];    //Initialises the Pieces map
        int x = 0;                      //Starts at 0,0 (top left)
        int y = 0;
        if (!this.white) {notation = new StringBuilder(notation).reverse().toString();}      //This will flip the notation to show black at the bottom


        for (Character c :               //Iterates through every character in the notation string
                notation.toCharArray()) {

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
                board[x][y] = new SimplePiece(Character.toLowerCase(c), x, y, white);        //Uses the factory method inside Piece to create a Piece of the correct class
                x++;    //increments X (moving right)
            }

        }
    }

    private void highlight(Graphics g, int x, int y) {
        //gate-keeping
        if(!possibleMoves.contains(x + String.valueOf(y))){ return;} //This location is irrelvant, skip

        Color highlightColor = spectator? new Color(200, 60, 0, 200) :     //Orange for spectator
                ((this.white == selectedPiece.white) && turn)?
                new Color(0, 200, 0, 200) : new Color(200, 0, 0, 200) ; //Green for current player, red for not current

        g.setColor(highlightColor);

        //If there is a piece at the location, the circle needs to be a lil bigger
        int circleSize = board[x][y] == null? 24: 44;
        int offset = board[x][y] == null? 20: 10;
        g.fillOval(x * SQUARE_SIZE + MARGIN + offset, y * SQUARE_SIZE + MARGIN + offset, circleSize, circleSize);
    }

    public String flipCoords(String Coords){        //flips coords from white layout to black layout or vice versa

        int x = Character.getNumericValue(Coords.charAt(0)) + 1;
        int y = Character.getNumericValue(Coords.charAt(1)) + 1;
        x = (9 - x) ;
        y = (9 - y) ;
        x--;
        y--;

        return x + String.valueOf(y);
    }

    public void loadPossibleMoves(List<String> newMoves){
        possibleMoves.clear();
        for (String s:
             newMoves) {
            possibleMoves.add(this.white ? s : flipCoords(s));
        }
    }

    public void setUpMouseListeners(JFrame frame) {

        frame.addMouseListener(new MouseInputListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!promotion) {return;}
                int mouseX = e.getX();
                int mouseY = e.getY();

                if (mouseX > 64 || mouseY < 0 || mouseY > 256 || mouseX < 0) {return;}  //If mouse it out of range, ignore

                char pieceType; //Holds the type to be returned to the board
                mouseY -= 32; //Accounts for border
                switch (mouseY / 64) {
                    case 0 -> pieceType = 'q';
                    case 1 -> pieceType = 'b';
                    case 2 -> pieceType = 'n';
                    case 3 -> pieceType = 'r';
                    default -> throw new RuntimeException("Invalid Piece");
                }
                pieceType = white ? Character.toUpperCase(pieceType) : pieceType;
                master.sendPromotion(pieceType);
                frame.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //Used for dragging
                if (promotion) {return;}
                //gets current mouse pos (and sends to server)
                int mX = mouseX = (e.getX() -MARGIN);
                int mY = mouseY = (e.getY()- (MARGIN + 32));


                //if mouse is out of range, ignore
                if (mY > BOARD_HEIGHT || mY < 0
                        || mX > BOARD_WIDTH || mX < 0) { return;}


                mX /= SQUARE_SIZE;
                mY /= SQUARE_SIZE;

                if (mX == 8 || mY >= 8) {return;}   //gate-keeping if mouse is out of range of array, but shouldn't happen

                selectedPiece = board[mX][mY];
                if (selectedPiece == null) {return;}      //not clicking a piece, exit here

                //Sends piece to server
                String location = selectedPiece.getLocation();

                if (!white) {location = flipCoords(location);}

                master.requestPossibleMoves(location);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (promotion) {return;}
                if (selectedPiece == null) {return;}      //gate-keeping

                //Gets mouse location as board coords
                int x = ((e.getX() - MARGIN)/ (SQUARE_SIZE));
                int y = ((e.getY() - (MARGIN + 32))/ (SQUARE_SIZE));
                String location = x + String.valueOf(y);
                String pieceLocation = selectedPiece.getLocation();

                if (possibleMoves.contains(location)) {   //Checks requested move is possible
                    if (!white) {
                        location = flipCoords(location);
                        pieceLocation = flipCoords(selectedPiece.getLocation());
                    }
                    master.requestMove(pieceLocation, location);
                }

                //clear relevant data and redraw board
                selectedPiece =null;
                possibleMoves.clear();
                frame.repaint();

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
                if (promotion) {return;}
                if (selectedPiece == null) {return;}
                mouseX = e.getX();
                mouseY = e.getY();
                //keep in range of Y
                if ( mouseY >  BOARD_HEIGHT+  MARGIN) { mouseY =  BOARD_HEIGHT+  MARGIN;}
                else if ( mouseY <  MARGIN) { mouseY =  MARGIN;}

                //Keep in range of X
                if ( mouseX >  BOARD_HEIGHT+  MARGIN) { mouseX =  BOARD_HEIGHT+  MARGIN;}
                else if ( mouseX <  MARGIN) { mouseX =  MARGIN;}

                //account for margin
                 mouseX -=  MARGIN;
                 mouseY -= ( MARGIN + 32);
                //redraw with new mouse location
                frame.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Leaving the game now may result in a loss.", "Quit?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    master.closeGame(false);
                    System.exit(0);
                }
            }
        });
    }

    public void notifyDisconnect() {
        if (JOptionPane.showConfirmDialog(frame,
                (spectator? "A player has disconnected" : "Your opponent has disconnected.") +
                        " They may return. \n Quit anyway?", "Quit?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
            master.closeGame(false);
            System.exit(0);
        }
    }

    public void endGame(String dialogue) {
       JOptionPane.showConfirmDialog(frame,
                dialogue,"Quit?",
               JOptionPane.DEFAULT_OPTION);
            master.closeGame(false);
            System.exit(0);
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

    public int getImageVal() {
        return type.BaseImageVal + (white? 0 : 6);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLocation(){

        return x + String.valueOf(y);
    }
}
