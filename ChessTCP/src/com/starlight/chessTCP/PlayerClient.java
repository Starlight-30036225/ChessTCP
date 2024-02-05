package com.starlight.chessTCP;

public class PlayerClient extends Client{
    public PlayerClient(String IP, int port) {
        super(IP, port);
    }


    @Override
    protected void HandlePacket(PacketHeader packetHeader) {
            //String String = readNextString();

            switch (packetHeader){
                case WELCOME:
                    System.out.println("readNextString");

                    sendMessage(PacketHeader.WELCOME, "");
                    break;
                case MOVE:
                    sendMessage(PacketHeader.TURN_PROMPT, "HI");
                    break;
                case BOARD_STATE:
                    break;
                case TURN_PROMPT:
                    break;
                case SELECT_PIECE:
                    break;
                case POSSIBLE_MOVES:
                    break;
                default:
                    break;


            }

    }
}
