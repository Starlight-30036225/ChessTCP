package com.starlight.chessTCP;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.MessageDigest;

public class PlayerMaster implements GameHandler, UIHandler{

    public BoardDisplay board;
    PlayerClient client;
    boolean requestNewPossibles = true;
    boolean started = false;
    public boolean done = false;

    MainMenu menu;

    public PlayerMaster() {
        ExecutorService pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 8201, this);
        pool.execute(client);

        board = new BoardDisplay(false, this);
        //Board.printMap();

    }
    //GameHander Interface
    public void receiveBoardStatus(String colourAndNotation) {
        String notation = colourAndNotation.substring(5);
        String colour = colourAndNotation.substring(0,5);
        if (!started){
            board.recievePlayerPreferences(menu.colourSelection, menu.spriteSelection);
            menu.frame.dispose();
            menu = null;
            board.printMap();
            started = true;
        }

        board.turn = (board.white == (Objects.equals(colour, "WHITE")));
        board.loadMapFromNotation(notation);
        board.frame.repaint();

    }
    public void receivePossibleMoves(List<String> possibleMoves) {

        board.loadPossibleMoves(possibleMoves);
        board.frame.repaint();
        requestNewPossibles = true;

    }
    public void receiveWelcomePack(String welcomePack) {
        switch (welcomePack.substring(0, 5)) {
            case "WHITE" -> board.white = true;
            case "BLACK" -> board.white = false;
            default -> {
                board.white = true;
                board.spectator = true;
            }

        }
    }
    public void receivePromotionPrompt(){

        board.promotion = !board.promotion;
        board.frame.repaint();

    }
    public void closeGame(String keepOpen) {
        if (keepOpen.equals("END")){
            if (board != null) {
                board.notifyShutdown();
            }
            if (menu != null) {
                menu.notifyShutdown();
            }
            client.shutdown();
            done = true;
        }
        else {
            board.notifyDisconnect();
        }
    }
    public void handleWin(String winner) {
        String dialogue = "";
        if (board.spectator) {
            dialogue = winner + " won the game!";
        }
        else{
            dialogue = board.white == winner.equals("WHITE")? "You won!" : "You lose...";
        }
        board.endGame(dialogue);
    }
    public void HandleRoomInfo(String roomList) {
        if (menu!= null) {
            menu.frame.dispose();
        }
        menu = new MainMenu(this, roomList);
    }


    //UIHandler Interface
    public void requestPossibleMoves(String location) {
        if (!requestNewPossibles) {return;}
        client.sendMessage(PacketHeader.SELECT_PIECE, location);

    }
    public void requestMove(String pieceLocation, String moveLocation){
        if (board.spectator) {requestNewPossibles = true; return;}
        client.sendMessage(PacketHeader.MOVE, pieceLocation + moveLocation);   //Sends selected piece and move in 1 string
        requestNewPossibles = true;
    }
    public void closeGame(boolean naturalEnd) {
        client.sendMessage(PacketHeader.DISCONNECT, "NULL");
        client.shutdown();
        done = true;
    }
    public void sendPromotion(char piece){
        client.sendMessage(PacketHeader.PROMOTION, String.valueOf(piece));   //Sends selected piece and move in 1 string
    }

    public void sendRoom(int selection, String password) {

        String Hash = password;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes());
            Hash = new String(messageDigest.digest());
        } catch (Exception ignored) {}



        client.sendMessage(PacketHeader.ROOM_INFO, selection - 1 + Hash);

    }

    public static void main(String[] args) {
        PlayerMaster master = new PlayerMaster();
        while (!master.done) {
        }
        System.exit(1);
    }
}


