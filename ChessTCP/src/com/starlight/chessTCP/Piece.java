package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    protected int x;
    protected int y;
    protected boolean white;
    public char characterVal;   //Used for notation

    Piece(char type, int x, int y, boolean white){
        this.characterVal = type;
        this.x = x;
        this.y = y;
        this.white = white;
    }

    public boolean move(String NewLocation){
        return false;
    }

    public abstract List<String> GetPossibleMoves(Piece[][] board);

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public static Piece CreatePiece(char type, int x, int y, boolean white) {

        switch (Character.toLowerCase(type)) {
            case 'p' -> {
                return new Pawn(type, x, y, white);
            }
            case 'n' -> {
                return new Knight(type, x, y, white);
            }
            case 'b' -> {
                return new Bishop(type, x, y, white);
            }
            case 'r' -> {
                return new Rook(type, x, y, white);
            }
            case 'q' -> {
                return new Queen(type, x, y, white);
            }
            case 'k' -> {
                return new King(type, x, y, white);
            }
            default -> {
                return null;
            }
        }
    }

    public boolean ValidateMove(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        if (!(x > 7 || x < 0 || y> 7 || y < 0)) {        //Checks the location is in range
            if (board[x][y] != null && board[x][y].white == piece.white) {return false;}  //If a piece in location is same colour, cant move there
            Moves.add(x + "" + y);
            return true;
        }
        return false;
    }
}

//DON'T LOOK AT THE INTERFACES FOR YOUR OWN SANITY
interface Diagonal{
    static List<String> GetPossibleMoves(Piece piece, Piece[][] board) {

        List<String> moves = new ArrayList<>(); //return val
        int testx = 0;
        int testy = 0;
        int distance = 0;
        {   //right

            do {
                distance++;
                testx = piece.x+distance;
                testy = piece.y+distance;
            }while (CheckEndOfPath(piece,board,testx,testy, moves));


            distance = 0;
            do {
                distance++;
                testx = piece.x+distance;
                testy = piece.y-distance;
            }while (CheckEndOfPath(piece,board,testx,testy, moves));
        }

        {   //left
            distance = 0;
            do {
                distance++;
                testx = piece.x-distance;
                testy = piece.y+distance;
            }while (CheckEndOfPath(piece,board,testx,testy, moves));


            distance = 0;
            do {
                distance++;
                testx = piece.x-distance;
                testy = piece.y-distance;
            }while (CheckEndOfPath(piece,board,testx,testy, moves));

        }
        return moves;
    }
    private static boolean CheckEndOfPath(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        return piece.ValidateMove(piece, board, x, y, Moves) &&
                (board[x][y] == null);  //If it gets here, move is valid. If space is not null, a piece is being taken so return false
    }
}

interface Straight{
    static List<String> GetPossibleMoves(Piece piece, Piece[][] board) {

        List<String> moves = new ArrayList<>(); //return val
        int testx = 0;
        int testy = 0;
        int distance = 0;
        do {
            distance++;
            testx = piece.x + distance;
            testy = piece.y;
        } while (CheckEndOfPath(piece, board, testx, testy, moves));


        distance = 0;
        do {
            distance++;
            testx = piece.x - distance;
            testy = piece.y;
        } while (CheckEndOfPath(piece, board, testx, testy, moves));


        distance = 0;
        do {
            distance++;
            testx = piece.x;
            testy = piece.y + distance;
        } while (CheckEndOfPath(piece, board, testx, testy, moves));


        distance = 0;
        do {
            distance++;
            testx = piece.x;
            testy = piece.y - distance;
        } while (CheckEndOfPath(piece, board, testx, testy, moves));

        return moves;
    }

    private static boolean CheckEndOfPath(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        return piece.ValidateMove(piece, board, x, y, Moves) &&
                (board[x][y] == null);  //If it gets here, move is valid. If space is not null, a piece is being taken so return false
    }
}

class Rook extends Piece implements Straight{


    Rook(char type,int x, int y, boolean white) {
        super(type,x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        return Straight.GetPossibleMoves(this, board);
    }
}

class Bishop extends Piece implements Diagonal{

    Bishop(char type,int x, int y, boolean white) {
        super(type,x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        return Diagonal.GetPossibleMoves(this, board);
    }
}

class Queen extends Piece implements Straight, Diagonal {


    Queen(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> moves = Straight.GetPossibleMoves(this, board);
        moves.addAll(Diagonal.GetPossibleMoves(this,board));
        return moves;
    }

}

class Pawn extends Piece{

    private boolean FirstMove = true;
    private int direction;
    Pawn(char type, int x, int y, boolean white) {
        super(type, x, y, white);
        direction = (white? -1 : 1);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();
        if (board[x][y+ direction] == null &&
                ValidateMove(this, board, x, y+ direction, Moves)) {
            Moves.add(x + "" + (y + direction));

        }
        //over stuffed can probs simplify
        if (FirstMove && board[x][y+ direction] == null && board[x][y+ 2 * direction] == null &&
                ValidateMove(this, board, x, y + direction * 2, Moves)) {
            Moves.add(x + "" + (y + direction * 2));
        }
        //Put en-passant rules here
        return Moves;
    }
}

class Knight extends Piece{
    Knight(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();
        ValidateMove(this,board, x + 2, y + 1, Moves);

        ValidateMove(this,board, x - 2, y + 1, Moves);

        ValidateMove(this,board, x + 2, y - 1, Moves);

        ValidateMove(this,board, x - 2, y - 1, Moves);

        ValidateMove(this,board, x + 1, y + 2, Moves);

        ValidateMove(this,board, x - 1, y + 2, Moves);

        ValidateMove(this,board, x + 1, y - 2, Moves);

        ValidateMove(this,board, x - 1, y - 2, Moves);

        return Moves;
    }
}

class King extends Piece{
    King(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        return null;
    }
}

