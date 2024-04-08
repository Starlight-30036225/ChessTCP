package com.starlight.chessTCP;

public interface UIHandler {

    void sendPromotion(char pieceType);

    void requestPossibleMoves(String location);

    void requestMove(String pieceLocation, String moveLocation);

    void closeGame(boolean naturalEnd);
}
