package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.List;

public class PlayerClient extends Client{
    public PlayerClient(String IP, int port, PlayerMaster Master) {
        super(IP, port);
        this.Master = Master;
        System.out.println(2 + "" + 2);
    }

    private PlayerMaster Master = null;
    @Override
    protected void HandlePacket(PacketHeader packetHeader) {
            //String String = readNextString();
            System.out.println(packetHeader);
            switch (packetHeader){
                case WELCOME:
                    sendMessage(PacketHeader.WELCOME, "hahhahaha");
                    break;
                case MOVE:
                    sendMessage(PacketHeader.TURN_PROMPT, "HI");
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
                default:
                    break;


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
