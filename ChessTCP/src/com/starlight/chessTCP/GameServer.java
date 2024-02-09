package com.starlight.chessTCP;

import java.util.List;

public class GameServer extends Server{

    GameMaster game;

    public GameServer(int port) {
        super(port);
        game = new GameMaster();
    }

    @Override
    public void HandlePacket(ConnectionHandler Handler, PacketHeader packetHeader) {
        System.out.println(packetHeader);
        switch (packetHeader){
            case WELCOME:
                System.out.println("Connected");
                break;
            case MOVE:
                Handler.sendMessage(PacketHeader.TURN_PROMPT, "HI");
                break;
            case BOARD_STATE:
                break;
            case TURN_PROMPT:
                break;
            case SELECT_PIECE:
                HandleSelect_Piece(Handler);
                break;
            case POSSIBLE_MOVES:
                break;
            default:
                break;


        }
    }

    private void HandleSelect_Piece(ConnectionHandler Handler) {
        String Location = Handler.readNextString();

        //seperate location into x and y components
        int x = Character.getNumericValue(Location.charAt(0));
        int y = Character.getNumericValue(Location.charAt(1));

        String CompositeMoveString = "";    //will hold all possible tiles the piece can move too
        for (String Move :game.getPossibleMoves(x,y)){  //loops through all moves found from pieces function
            CompositeMoveString += Move;

        }
        //sends all moves back to client
        Handler.sendMessage(PacketHeader.POSSIBLE_MOVES, CompositeMoveString);
    }

    @Override
    protected void SendWelcomeMessage(ConnectionHandler CH) {
        CH.sendMessage(PacketHeader.BOARD_STATE, game.LoadNotationFromMap());
    }


}

class GameMaster {

    Piece[][] board;
    public GameMaster(){
        LoadMapFromNotation("rnbqkbnr/1pppppp1/8/3rR3/3Rr3/8/7P/RNBQKBNR");
        //LoadMapFromNotation("8/4q3/8/44/8/8/5R2/8");
    }

    private void LoadMapFromNotation(String Notation) {
        board = new Piece[8][8];    //Initialises the Pieces map
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
                board[x][y] = Piece.CreatePiece(c, x, y, white);        //Uses the factory method inside Piece to create a Piece of the correct class
                x++;    //increments X (moving right)
            }

        }
    }

    public String LoadNotationFromMap() {
        String Notation = "";
        int counter = 0;
        for(int file = 0; file < 8; file++){
            counter = 0;
            for (int rank = 0; rank <8; rank++) {
                Piece temp = board[rank][file];

                if (temp == null) {
                    counter++;
                    continue;
                }
                if (counter > 0){
                    Notation += counter;
                    counter = 0;
                }
                Notation += temp.characterVal;
            }
            if (counter > 0){
                Notation += counter;
                counter = 0;
            }
            if (file < 7) {
                Notation += "/";

            }
        }
        return Notation;
    }

    public List<String> getPossibleMoves(int x, int y){

        Piece temp = board[x][y];
        return temp.GetPossibleMoves(board);
    }
}