package com.starlight.chessTCP;

import java.util.List;

public interface GameHandler {

    void receiveBoardStatus(String ColourAndNotation);

    void receivePossibleMoves(List<String> possibleMoves);

    void receiveWelcomePack(String WelcomePack);

    void receivePromotionPrompt();

}
