package com.starlight.chessTCP;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerMaster implements GameHandler{

    public BoardDisplay board;
    PlayerClient client;
    boolean requestNewPossibles = true;
    boolean started = false;
    public PlayerMaster() {
        ExecutorService pool = Executors.newCachedThreadPool();
        client = new PlayerClient("127.0.0.1", 9999, this);
        pool.execute(client);

        board = new BoardDisplay(false, this);
        //Board.printMap();

    }
    public void receiveBoardStatus(String colourAndNotation) {
        String notation = colourAndNotation.substring(5);
        String colour = colourAndNotation.substring(0,5);
        if (!started){
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
    }
    public void requestPossibleMoves(String location) {
        if (!requestNewPossibles) {return;}
        client.sendMessage(PacketHeader.SELECT_PIECE, location);

    }
    public void requestMove(String pieceLocation, String moveLocation){
        if (board.spectator) {requestNewPossibles = true; return;}
        client.sendMessage(PacketHeader.MOVE, pieceLocation + moveLocation);   //Sends selected piece and move in 1 string
        requestNewPossibles = true;
    }
    public void sendPromotion(char piece){
        client.sendMessage(PacketHeader.PROMOTION, String.valueOf(piece));   //Sends selected piece and move in 1 string
    }
    public static void main(String[] args){new PlayerMaster();}
}


