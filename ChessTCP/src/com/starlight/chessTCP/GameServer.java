package com.starlight.chessTCP;
import java.util.*;

public class GameServer extends Server{

    Map<ConnectionHandler, GameMaster> roomMap; //connects clients to their respective rooms
    public GameServer(int port) {
        super(port);
        roomMap = new HashMap<>();
    }
    @Override
    public void handlePacket(ConnectionHandler handler, PacketHeader packetHeader) {
        GameMaster room = roomMap.get(handler);
        //System.out.println(packetHeader);
        switch (packetHeader){
            case WELCOME -> {
                System.out.println(handler.ID + " connected.");
            }
            case MOVE -> {
                handleMove(handler, room);
            }
            case SELECT_PIECE -> {
                handleSelect_Piece(handler, room);
            }
            case ROOM_INFO -> {
                assignClientRoom(handler);
            }
            case PROMOTION -> {
                handlePromotion(handler, room);
            }
            case DISCONNECT -> {
                handleDisconnect(handler, room);
            }
        }
    }
    void handleMove(ConnectionHandler handler, GameMaster room){
        if (room.promotion != null) {return;}   //If a pawn is waiting to be promoted, no moves can be made

        if (handler !=
                (room.white? room.whitePlayer :room.blackPlayer)) {return;}     //This move was requested by a spectator, ignore.


        String Composite = handler.readNextString();

        //gets the start location of the move/piece location
        int pieceX = Character.getNumericValue(Composite.charAt(0));
        int pieceY = Character.getNumericValue(Composite.charAt(1));

        if (room.board[pieceX][pieceY] == null ||
                (room.board[pieceX][pieceY].white != room.white)) {return;} //Piece is invalid or wrong colour

        //gets the end location of the move
        int moveX = Character.getNumericValue(Composite.charAt(2));
        int moveY = Character.getNumericValue(Composite.charAt(3));

        if (!room.board[pieceX][pieceY].move(room.board, moveX+ String.valueOf(moveY))) {return;}   //move failed, exit here

        //Check if moved piece is a pawn valid for promotion
        if (checkPromotion(handler, room, moveX, moveY)) return;    //A pawn is waiting to be promoted, exit here so turn doesnt change

        room.white = !room.white;   //flips the current players turn

        //Update all clients of the move
        clients.stream()
                .filter(CH -> roomMap.get(CH) == room)
                .toList().forEach(CH ->
                        CH.sendMessage(PacketHeader.BOARD_STATE, (room.white ? "WHITE" : "BLACK") + room.loadNotationFromMap()));

        checkGameOver(room);
    }

    private static boolean checkPromotion(ConnectionHandler handler, GameMaster room, int moveX, int moveY) {
        if (room.board[moveX][moveY].getClass() == Pawn.class) {    //Piece is pawn
            Pawn p = (Pawn) room.board[moveX][moveY];

            if (((p.direction + p.y > 7) || (p.direction + p.y) < 0)) { //Piece is at edge of board
                room.promotion = p;
                handler.sendMessage(PacketHeader.PROMOTION,"null");
                return true;
            }
        }
        return false;
    }

    private void checkGameOver(GameMaster room) {
        String winner = room.checkGameState();
        if (winner.equals("")) {return;}    //No player is in check, exit here

        clients.stream()
                .filter(CH -> roomMap.get(CH) == room)
                .toList().forEach(CH ->
                        CH.sendMessage(PacketHeader.WIN, winner));
    }

    void handleSelect_Piece(ConnectionHandler handler, GameMaster room) {
        String location = handler.readNextString();

        //separate location into x and y components
        int x = Character.getNumericValue(location.charAt(0));
        int y = Character.getNumericValue(location.charAt(1));

        String CompositeMoveString = "";    //will hold all possible tiles the piece can move too
        for (String Move :room.getPossibleMoves(x,y)){  //loops through all moves found from pieces function
            CompositeMoveString += Move;
        }
        //sends all moves back to client
        handler.sendMessage(PacketHeader.POSSIBLE_MOVES, CompositeMoveString);
    }
    void handlePromotion(ConnectionHandler handler, GameMaster room) {
        roomMap.get(handler).promotePawn(handler.readNextString());
        handler.sendMessage(PacketHeader.PROMOTION, "NULL");

        room.white = !room.white;   //Now promotion has happened, flip turn

        clients.stream()
                .filter(CH -> roomMap.get(CH) == room)
                .toList().forEach(CH ->
                        CH.sendMessage(PacketHeader.BOARD_STATE, (room.white ? "WHITE" : "BLACK") + room.loadNotationFromMap()));

        checkGameOver(room);    //This could be the final move, check
    }
    void sendRoomInfo(ConnectionHandler handler, boolean retry) {
        List<GameMaster> allRooms = roomMap.values().stream().distinct().toList(); //Creates a non-repeating list of all rooms, probably broken
        String infoString = retry? "Y":"N";

        for (GameMaster tempRoom:
                allRooms) {
            String temp;
            temp = tempRoom.connections + "_" + (Objects.equals(tempRoom.password, "") ? 'N' : 'Y');
            infoString += temp;
        }

        if (infoString.equals("")) {infoString = "...";}    //Don't want to risk sending an empty packet.

        handler.sendMessage(PacketHeader.ROOM_INFO, infoString);
    }
    void assignClientRoom(ConnectionHandler handler) {
        String roomSelection = handler.readNextString();
        GameMaster room;
        String password = "";

        int selection = roomSelection.charAt(0) - 48; //gets numerical value, not ascii

        if(selection == -3){    //player is requesting an updated list (Not yet implemented)
            sendRoomInfo(handler, false);
            return;
        }

        if (roomSelection.length() > 1) //If string is longer than 1 char, there is a password attached.
        {
            password = roomSelection.substring(1);
        }

        if (selection == 0){    //Client is requesting a new room
            room = new GameMaster();
            room.password = password;   //If this is empty, it is ignored henceforth
        }
        else{   //Player is attempting to join an existing room
            room = roomMap.values().stream().distinct().toList().get(selection - 1);
            if (!Objects.equals(room.password, password)){
                handler.sendMessage(PacketHeader.MISC, "Incorrect Password.");
                sendRoomInfo(handler, true);
                return;
            }
        }

        //Now a room has been created/chosen select the clients role:

        if (room.whitePlayer == null) {
            room.whitePlayer = handler;
            handler.sendMessage(PacketHeader.WELCOME, "WHITE");
        } else if (room.blackPlayer == null) {
            room.blackPlayer = handler;
            handler.sendMessage(PacketHeader.WELCOME, "BLACK");
        } else {
            handler.sendMessage(PacketHeader.WELCOME, "SPECT");
        }

        roomMap.put(handler, room);
        room.connections++;

        if(room.connections == 2 && !room.started) {        //Waits until both core players have joined to start game
            for (ConnectionHandler CH:
                    clients.stream()
                            .filter(CH -> roomMap.get(CH) == room)
                            .toList()) {
                CH.sendMessage(PacketHeader.BOARD_STATE, "WHITE" + room.loadNotationFromMap());
                room.started = true;

            }
        } else if (room.started){        //Anyone who joins after the game has started, even a rejoin, doesn't have to wait
            handler.sendMessage(PacketHeader.BOARD_STATE, (room.white? "WHITE" : "BLACK") + room.loadNotationFromMap());
        }



    }

    void handleDisconnect(ConnectionHandler handler, GameMaster room) {
        room.connections--;
        if (handler == room.whitePlayer) {room.whitePlayer = null;}
        if (handler == room.blackPlayer) {room.blackPlayer = null;}

        roomMap.remove(handler);    //removes all references to the disconnecting player

        boolean CloseRoom = (room.whitePlayer == null && room.blackPlayer == null); //If both players have left, close the room.

        if (room.whitePlayer != null && room.blackPlayer != null) {return;} //the player that left was a spectator,doesn't matter

        //tells other clients that this player has left
        for (ConnectionHandler CH:
                clients.stream()
                        .filter(CH -> roomMap.get(CH) == room)
                        .toList()) {
            CH.sendMessage(PacketHeader.DISCONNECT, CloseRoom? "END": "CON"); //If no players, close room, otherwise keep open till both players leave
            if (CloseRoom) {
                roomMap.remove(CH);}
        }


    }
    @Override
    protected void sendWelcomeMessage(ConnectionHandler handler) {
        sendRoomInfo(handler, false);
    }

}

class GameMaster {

    public boolean started = false;
    Piece[][] board;
    boolean white = true;
    public Server.ConnectionHandler whitePlayer;
    public Server.ConnectionHandler blackPlayer;
    public Integer connections;
    public Pawn promotion;  //Holds a pawn waiting to be prompted, mostly null
    String password = "";
    public GameMaster(){
        connections = 0;
        //loadMapFromNotation("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        loadMapFromNotation("k7/6RP/8/8/8/8/p7/7K");
    }

    private void loadMapFromNotation(String notation) {
        board = new Piece[8][8];    //Initialises the Pieces map
        int x = 0;                      //Starts at 0,0 (top left)
        int y = 0;

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
                board[x][y] = Piece.createPiece(c, x, y, white);        //Uses the factory method inside Piece to create a Piece of the correct class
                x++;    //increments X (moving right)
            }

        }
    }

    public String loadNotationFromMap() {       //constructs a FEN string denoting the current state of board
        String notation = "";   //return val

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
                    notation += counter;        //add the number of spaces before piece to notation
                    counter = 0;        //Reset counter
                }
                notation += temp.characterVal;  //add piece as char to notation
            }
            if (counter > 0){
                notation += counter;
            }
            if (file < 7) {
                notation += "/";

            }
        }
        return notation;
    }

    public List<String> getPossibleMoves(int x, int y){
        Piece temp = board[x][y];
        return temp.getLegalMoves(board);
    }

    public void promotePawn(String pieceInfo) {
        boolean white = Character.isUpperCase(pieceInfo.charAt(0));       //Capital is a white piece, lower case is black
        board[promotion.x][promotion.y] = Piece.createPiece(pieceInfo.charAt(0), promotion.x, promotion.y,white);
        promotion = null;   //pawn has been promoted, no need to hold anything here anymore
    }

    public String checkGameState(){ //returns winning player
        boolean whiteLegalMove = false;
        boolean blackLegalMove = false;

        //itterates through all pieces to see if both players have a possible move
        for(int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {

                Piece temp = board[rank][file]; //Gets piece at location

                if (temp == null) {continue;}   //No piece here

                //Stops redundant checks, only one legal move needs to be found
                if (temp.white && whiteLegalMove) {continue;}
                if (!temp.white && blackLegalMove) {continue;}


                if (temp.white) {   whiteLegalMove  = !temp.getLegalMoves(board).isEmpty();}
                else {blackLegalMove  = !temp.getLegalMoves(board).isEmpty();}

                if (whiteLegalMove && blackLegalMove) {break;}  //stops searching when legal moves have been found
            }
            if (whiteLegalMove && blackLegalMove) {break;}  //Can only break one loop at a time, and there is two. Forgive the repetition
        }

        //returns the opposite player, AKA the winner
        if (!whiteLegalMove) {
            return "BLACK";
        } else if (!blackLegalMove) {
            return "WHITE";
        }
        return "";
    }
}
