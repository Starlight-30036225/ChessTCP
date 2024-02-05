package com.starlight.chessTCP;

public enum PieceEnum {

    NONE(-1),
    p(5),
    r(4),
    n(3),
    b(2),
    q(1),
    k(0);

    public int BaseImageVal;
    PieceEnum(int BaseImageVal) {
        this.BaseImageVal = BaseImageVal;
    }
}
