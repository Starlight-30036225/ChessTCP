package com.starlight.chessTCP;
import java.util.*;
import java.util.stream.Stream;

public class GameServer extends Server{

    //GameMaster game;

    Map<ConnectionHandler, GameMaster> roomMap;



    boolean testmode = false;

    public GameServer(int port) {
        super(port);
        roomMap = new HashMap<>();
        //game = new GameMaster();
    }

    @Override
    public void HandlePacket(ConnectionHandler Handler, PacketHeader packetHeader) {
        GameMaster room = roomMap.get(Handler);
        System.out.println(packetHeader);
        switch (packetHeader){
            case WELCOME:
                System.out.println("Connected");
                break;
            case MOVE:
                HandleMove(Handler, room);
                break;
            case BOARD_STATE:
                break;
            case TURN_PROMPT:
                break;
            case SELECT_PIECE:
                HandleSelect_Piece(Handler, room);
                break;
            case POSSIBLE_MOVES:
                break;
            case ROOM_INFO:
                AssignClientRoom(Handler);
            default:
                break;


        }
    }
    private void HandleMove(ConnectionHandler Handler, GameMaster room){

        String Composite = Handler.readNextString();

        if (!testmode && Handler !=
                (room.white? room.WhitePlayer :room.BlackPlayer)) {return;}

        //gets the start location of the move/piece location
        int Piecex = Character.getNumericValue(Composite.charAt(0));
        int Piecey = Character.getNumericValue(Composite.charAt(1));

        if (room.board[Piecex][Piecey] == null ||
                (!testmode && room.board[Piecex][Piecey].white != room.white)) {return;} //Piece is invalid or wrong colour

        //gets the end location of the move
        int Movex = Character.getNumericValue(Composite.charAt(2));
        int Movey = Character.getNumericValue(Composite.charAt(3));

        if (!room.board[Piecex][Piecey].move(room.board,Movex+ "" + Movey)) {return;}   //move failed, exit here
        for (ConnectionHandler H:
                clients) {
            if (roomMap.get(H) != room){continue;}  //GateKeeping --- Needs improvement
            H.sendMessage(PacketHeader.BOARD_STATE, room.LoadNotationFromMap());    //Update all clients of the move
        }

        if (testmode) {return;}
        room.white = !room.white;


    }
    private void HandleSelect_Piece(ConnectionHandler Handler, GameMaster room) {
        String Location = Handler.readNextString();

        //seperate location into x and y components
        int x = Character.getNumericValue(Location.charAt(0));
        int y = Character.getNumericValue(Location.charAt(1));

        String CompositeMoveString = "";    //will hold all possible tiles the piece can move too
        for (String Move :room.getPossibleMoves(x,y)){  //loops through all moves found from pieces function
            CompositeMoveString += Move;

        }
        //sends all moves back to client
        Handler.sendMessage(PacketHeader.POSSIBLE_MOVES, CompositeMoveString);
    }

    protected void SendRoomInfo(ConnectionHandler Handler) {
        List<GameMaster> AllRooms = roomMap.values().stream().distinct().toList();
        String infoString = "";
        for (GameMaster temproom:
                AllRooms) {
            String temp;
            temp = temproom.Connections + "_" + (Objects.equals(temproom.password, "") ? 'N' : 'Y');
            infoString += temp;
        }
        if (infoString.equals("")) {infoString = "...";}
        Handler.sendMessage(PacketHeader.ROOM_INFO, infoString);
    }

    private void AssignClientRoom(ConnectionHandler Handler) {
        String RoomSelection = Handler.readNextString();
        GameMaster room = null;
        String password = "";
        int selection = RoomSelection.charAt(0) - 48;
        if (RoomSelection.length() > 1) {password = RoomSelection.substring(1);}
        if (selection == 0){
            room = new GameMaster();
            room.password = password;
        }
        else{
            room = roomMap.values().stream().distinct().toList().get(selection - 1);
            if (!Objects.equals(room.password, password)){
                Handler.sendMessage(PacketHeader.MISC, "Incorrect Password.");
                SendRoomInfo(Handler);
                return;
            }
        }


        if (!testmode) {
            if (room.WhitePlayer == null) {
                room.WhitePlayer = Handler;
                roomMap.put(Handler, room);
                Handler.sendMessage(PacketHeader.WELCOME, "WHITE");
            } else if (room.BlackPlayer == null) {
                room.BlackPlayer = Handler;
                roomMap.put(Handler, room);
                Handler.sendMessage(PacketHeader.WELCOME, "BLACK");
            }
        }
        room.Connections++;

        if(room.Connections > 1) {
            for (ConnectionHandler CH:
                    clients) {
                if (roomMap.get(CH) != room) {
                    continue;
                }  //GateKeeping --- Needs improvement
                CH.sendMessage(PacketHeader.BOARD_STATE, room.LoadNotationFromMap());
            }
        }
    }

    @Override
    protected void SendWelcomeMessage(ConnectionHandler Handler) {
        SendRoomInfo(Handler);
    }
}

class GameMaster {

    Piece[][] board;

    boolean white = true;

    public Server.ConnectionHandler WhitePlayer;
    public Server.ConnectionHandler BlackPlayer;

    public Integer Connections;
    String password = "";
    public GameMaster(){
        Connections = 0;
        LoadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
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
