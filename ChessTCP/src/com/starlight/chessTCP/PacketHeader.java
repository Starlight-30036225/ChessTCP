package com.starlight.chessTCP;

public enum PacketHeader {

    WELCOME,
    BOARD_STATE,
    SELECT_PIECE,
    POSSIBLE_MOVES,
    MOVE,
    PROMOTION,
    ROOM_INFO,
    DISCONNECT,
    WIN

}

