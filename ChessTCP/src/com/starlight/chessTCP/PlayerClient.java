package com.starlight.chessTCP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class PlayerClient extends Client{
    public PlayerClient(String IP, int port, PlayerMaster Master) {
        super(IP, port);
        this.Master = Master;
    }
    private PlayerMaster Master;        //Reference to owner
    @Override
    protected void HandlePacket(PacketHeader packetHeader) {
            System.out.println(packetHeader);
            switch (packetHeader){
                case WELCOME:
                    Master.receiveWelcomePack(readNextString());
                    break;
                case MOVE:
                    break;
                case BOARD_STATE:
                    Master.recieveBoardStatus(readNextString());
                    break;
                case TURN_PROMPT:
                    break;
                case SELECT_PIECE:
                    break;
                case POSSIBLE_MOVES:
                    SeperateMoveList();
                    break;
                case ROOM_INFO:
                    SelectRoom();
                    break;
                case MISC:
                    System.out.println(readNextString());
                    break;
                case PROMOTION:
                    readNextString(); //Clear buffer
                    Master.RecievePromotionPrompt();
                    break;
                default:
                    break;


            }

    }

    private void SelectRoom(){
        Scanner sc = new Scanner(System.in); //System.in is a standard input stream
        String Rooms = readNextString();
        String Password = "";
        int selection;
        System.out.println("1: Create new room");
        if (!Objects.equals(Rooms, "...")) {
            for (int i = 0; i < Rooms.length() / 3; i++) {
                System.out.println((i + 2) + ": " + Rooms.charAt(i * 3) + " Connected - " +
                        (Rooms.charAt((i * 3) + 2) == (char) 'Y' ? "Locked" : "Open"));
            }
        }
        while (true) {
            try {
                selection = Integer.parseInt(sc.nextLine());              //reads string
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection");
                continue;
            }
            if (selection > (Rooms.length() / 3) + 2 || selection < 0){
                System.out.println("Invalid selection");
                continue;
            }
            if (selection == 1){
                System.out.println("Enter password, leave black for no password:");
                Password = sc.nextLine();
            } else if (Rooms.charAt(((selection - 2) * 3) + 2) == (char) 'Y'){
                System.out.println("Enter password");
                Password = sc.nextLine();
            }
            sendMessage(PacketHeader.ROOM_INFO, selection - 1 + Password);
            return;
        }
    }

    private void SeperateMoveList() {

        //move list is recieved as one long string
        List<String> MoveList = new ArrayList<>();
        String PossibleMoves = readNextString();

        for (int i = 0; i < PossibleMoves.length() - 1; i += 2){
            MoveList.add(PossibleMoves.substring(i, i+2));  //takes every set of 2 characters and adds them to the list
        }
        Master.receivePossibleMoves(MoveList);  //sends resulting list to master object
    }
}
