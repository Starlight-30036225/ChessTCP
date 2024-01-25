package com.starlight.chessTCP;

public class PlayerClient extends Client{
    public PlayerClient(String IP, int port) {
        super(IP, port);
    }


    private void HandlePacket(PacketHeader packetHeader) {
            String String = readNextString();

            switch (PacketHeader.valueOf(String)){

                case MOVE:
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
            sendMessage(PacketHeader.MOVE, String);

    }
}
