package com.starlight.chessTCP;

import java.util.List;

public interface GameHandler {

    void receiveBoardStatus(String colourAndNotation);

    void receivePossibleMoves(List<String> possibleMoves);

    void receiveWelcomePack(String welcomePack);

    void receivePromotionPrompt();

    void closeGame(String keepOpen);

    void handleWin(String losingPlayer);
}

