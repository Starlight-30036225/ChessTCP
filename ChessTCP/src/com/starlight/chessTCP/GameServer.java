package com.starlight.chessTCP;

public class GameServer extends Server{

    public GameServer(int port) {
        super(port);
    }

    @Override
    public void HandlePacket(ConnectionHandler Handler, PacketHeader packetHeader) {
        switch (packetHeader){
            case WELCOME:
                System.out.println("Connected");
                break;
            case MOVE:
                Handler.sendMessage(PacketHeader.TURN_PROMPT, "HI");
                break;
            case BOARD_STATE:
                break;
            case TURN_PROMPT:
                break;
            case SELECT_PIECE:
                System.out.println(Handler.readNextString() + "  ");
                break;
            case POSSIBLE_MOVES:
                break;
            default:
                break;


        }
    }

    @Override
    protected void SendWelcomeMessage(ConnectionHandler CH) {
        CH.sendMessage(PacketHeader.WELCOME, "WELCOME TO THE GAME: PLAYER");
    }


}
