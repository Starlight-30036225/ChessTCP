package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class PlayerClient extends Client{

    private final GameHandler master;        //Reference the interface of the owner
    public PlayerClient(String IP, int port, GameHandler Master) {
        super(IP, port);
        this.master = Master;
    }
    @Override
    protected void handlePacket(PacketHeader packetHeader) {
        System.out.println(packetHeader);
        switch (packetHeader) {
            case WELCOME -> master.receiveWelcomePack(readNextString());

            case BOARD_STATE -> master.receiveBoardStatus(readNextString());

            case POSSIBLE_MOVES -> separateMoveList();

            case ROOM_INFO -> selectRoom();

            case MISC -> System.out.println(readNextString());

            case PROMOTION -> {
                readNextString(); //Clear buffer
                master.receivePromotionPrompt();
            }
            case DISCONNECT -> {
                master.closeGame(readNextString());
                //shutdown();
            }
            case WIN -> {
                master.handleWin(readNextString());

            }
            default -> {
            }
        }

    }

    private void selectRoom(){
        Scanner sc = new Scanner(System.in); //System.in is a standard input stream
        String roomList = readNextString();
        String password = "";
        int selection;
        System.out.println("1: Create new room");
        if (!Objects.equals(roomList, "...")) {
            for (int i = 0; i < roomList.length() / 3; i++) {
                System.out.println((i + 2) + ": " + roomList.charAt(i * 3) + " Connected - " +
                        (roomList.charAt((i * 3) + 2) == 'Y' ? "Locked" : "Open"));
            }
        }
        while (true) {
            try {
                selection = Integer.parseInt(sc.nextLine());              //reads string
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection");
                continue;
            }
            if (selection > (roomList.length() / 3) + 2 || selection < 0){
                System.out.println("Invalid selection");
                continue;
            }
            if (selection == 1){
                System.out.println("Enter password, leave black for no password:");
                password = sc.nextLine();
            } else if (roomList.charAt(((selection - 2) * 3) + 2) == 'Y'){
                System.out.println("Enter password");
                password = sc.nextLine();
            }
            sendMessage(PacketHeader.ROOM_INFO, selection - 1 + password);
            return;
        }
    }

    private void separateMoveList() {
        //move list is received as one long string
        List<String> moveList = new ArrayList<>();
        String possibleMoves = readNextString();

        for (int i = 0; i < possibleMoves.length() - 1; i += 2){
            moveList.add(possibleMoves.substring(i, i+2));  //takes every set of 2 characters and adds them to the list
        }
        master.receivePossibleMoves(moveList);  //sends resulting list to master object
    }
}
