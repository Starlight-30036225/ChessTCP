package com.starlight.chessTCP;
import java.util.List;

public class GameServer extends Server{

    GameMaster game;

    ConnectionHandler WhitePlayer;
    ConnectionHandler BlackPlayer;

    boolean white = true;

    boolean testmode = false;

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
                HandleMove(Handler);
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
    private void HandleMove(ConnectionHandler Handler){
        String Composite = Handler.readNextString();

        if (!testmode && Handler !=
                (white? WhitePlayer :BlackPlayer)) {return;}

        //gets the start location of the move/piece location
        int Piecex = Character.getNumericValue(Composite.charAt(0));
        int Piecey = Character.getNumericValue(Composite.charAt(1));

        if (game.board[Piecex][Piecey] == null ||
                (!testmode && game.board[Piecex][Piecey].white != white)) {return;} //Piece is invalid or wrong colour

        //gets the end location of the move
        int Movex = Character.getNumericValue(Composite.charAt(2));
        int Movey = Character.getNumericValue(Composite.charAt(3));

        if (!game.board[Piecex][Piecey].move(game.board,Movex+ "" + Movey)) {return;}   //move failed, exit here
        for (ConnectionHandler H:
                clients) {
            H.sendMessage(PacketHeader.BOARD_STATE, game.LoadNotationFromMap());    //Update all clients of the move
        }

        if (testmode) {return;}
        white = !white;


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



        if (!testmode) {
            if (WhitePlayer == null) {
                WhitePlayer = CH;
                CH.sendMessage(PacketHeader.WELCOME, "WHITE");
            } else if (BlackPlayer == null) {
                BlackPlayer = CH;
                CH.sendMessage(PacketHeader.WELCOME, "BLACK");
            }
        }
        CH.sendMessage(PacketHeader.BOARD_STATE, game.LoadNotationFromMap());

    }
}

class GameMaster {

    Piece[][] board;
    public GameMaster(){
        LoadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        //LoadMapFromNotation("rnbqkbnr/1pppppp1/8/3rR3/3Rr3/8/7P/RNBQKBNR");
        //LoadMapFromNotation("3k4/8/8/8/8/8/8/R3K2R");
        //LoadMapFromNotation("3k4/8/8/8/3Q4/8/8/4K3");
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

    public String LoadNotationFromMap() {       //constructs a FEN string denoting the current state of board
        String Notation = "";   //return val

        int counter;
        for(int file = 0; file < 8; file++){
            counter = 0;
            for (int rank = 0; rank <8; rank++) {
                Piece temp = board[rank][file]; //Gets piece at location
                if (temp == null) { //there is no piece at location
                    counter++;
                    continue;
                }

                if (counter > 0){   //if the counter is more than 1, and a piece has been found
                    Notation += counter;        //add the number of spaces before piece to notation
                    counter = 0;        //Reset counter
                }
                Notation += temp.characterVal;  //add piece as char to notation
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
        return temp.GetLegalMoves(board);
    }
}
