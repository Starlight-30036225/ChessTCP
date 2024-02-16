package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Piece {

    protected int x;
    protected int y;
    protected boolean white;
    public char characterVal;   //Used for notation

    Piece(char type, int x, int y, boolean white) {
        this.characterVal = type;
        this.x = x;
        this.y = y;
        this.white = white;
    }

    public boolean move(Piece[][] board,String NewLocation) {
        if (GetPossibleMoves(board).contains(NewLocation)){
            int oldx = x;
            int oldy = y;
            x = Character.getNumericValue(NewLocation.charAt(0));
            y = Character.getNumericValue(NewLocation.charAt(1));
            board[x][y] = this;
            board[oldx][oldy] = null;
            return true;
        }
        return false;
    }

    public abstract List<String> GetPossibleMoves(Piece[][] board);

    public List<String> GetLegalMoves(Piece[][] board) {
        King k = null;
        for (Piece[] row:
                board){
            for (Piece p:
                row){
                if (p != null && p.white == white && p.getClass() == King.class){
                    k = (King)p;
                    break;
                }
            }
            if (k!=null){break;}
        }
        List<String> LegalMoves = new ArrayList<>();
        for (String Move:
                this.GetPossibleMoves(board)) {
            if (SimulateMove(board, this, Character.getNumericValue(Move.charAt(0)),Character.getNumericValue(Move.charAt(1)), k)) {
                LegalMoves.add(Move);
            }
        }

        return LegalMoves;
    }

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
        if (!(x > 7 || x < 0 || y > 7 || y < 0)) {        //Checks the location is in range
            if (board[x][y] != null && board[x][y].white == piece.white) {
                return false;
            }  //If a piece in location is same colour, cant move there
            Moves.add(x + "" + y);
            return true;
        }
        return false;
    }

    static boolean isLocationSafe(Piece[][] Board, Piece piece, int x, int y) {
        for (Piece[] col :
                Board) {
            for (Piece p :
                    col) {  //Iterates through all pieces
                if (p != null && piece.white != p.white && p.getClass() != King.class &&
                        p.GetPossibleMoves(Board).contains(x + "" + y)) {
                    return false;
                }
            }

        }
        return true;
    }

    static boolean SimulateMove(Piece[][] Board, Piece piece, int x, int y, King king) {
        int defaultX = piece.x;
        int defaultY = piece.y; //Use to return the piece to its start location after checks

        //HACK?
        Piece TemporaryStorage = Board[x][y]; //Gets the piece at the testing location if this move is a capture

        Board[defaultX][defaultY] = null;     //sets the current space to empty
        Board[x][y] = piece;       //Sets the piece to the new location

        piece.x = x;
        piece.y = y;     //Ignores legality checks and forces the move

        // check if the king is safe
        boolean locationSafe = isLocationSafe(Board, king, king.x, king.y);

        //resets the locations of test piece and potential capture
        Board[defaultX][defaultY] = piece;
        Board[x][y] = TemporaryStorage;

        piece.x = defaultX;
        piece.y = defaultY;
        return locationSafe;
    }
}

//DON'T LOOK AT THE INTERFACES FOR YOUR OWN SANITY
interface Diagonal {
    static List<String> GetPossibleMoves(Piece piece, Piece[][] board) {

        List<String> moves = new ArrayList<>(); //return val
        int testx = 0;
        int testy = 0;
        int distance = 0;
        {   //right

            do {
                distance++;
                testx = piece.x + distance;
                testy = piece.y + distance;
            } while (CheckEndOfPath(piece, board, testx, testy, moves));


            distance = 0;
            do {
                distance++;
                testx = piece.x + distance;
                testy = piece.y - distance;
            } while (CheckEndOfPath(piece, board, testx, testy, moves));
        }

        {   //left
            distance = 0;
            do {
                distance++;
                testx = piece.x - distance;
                testy = piece.y + distance;
            } while (CheckEndOfPath(piece, board, testx, testy, moves));


            distance = 0;
            do {
                distance++;
                testx = piece.x - distance;
                testy = piece.y - distance;
            } while (CheckEndOfPath(piece, board, testx, testy, moves));

        }
        return moves;
    }

    private static boolean CheckEndOfPath(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        return piece.ValidateMove(piece, board, x, y, Moves) &&
                (board[x][y] == null);  //If it gets here, move is valid. If space is not null, a piece is being taken so return false
    }
}

interface Straight {
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

class Rook extends Piece implements Straight {


    Rook(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        return Straight.GetPossibleMoves(this, board);
    }
}

class Bishop extends Piece implements Diagonal {

    Bishop(char type, int x, int y, boolean white) {
        super(type, x, y, white);
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
        moves.addAll(Diagonal.GetPossibleMoves(this, board));
        return moves;
    }

}

class Pawn extends Piece {

    private boolean FirstMove = true;
    private boolean Vulnerable = false;
    private int direction;

    Pawn(char type, int x, int y, boolean white) {
        super(type, x, y, white);
        direction = (white ? -1 : 1);
    }

    @Override
    public boolean move(Piece[][] board, String NewLocation) {

        int newx = Character.getNumericValue(NewLocation.charAt(0));
        int newy = Character.getNumericValue(NewLocation.charAt(1));
        //I dont need to use too many checks here, as the only chance to move into an empty space with a piece behind it is en-passant
        boolean TryingToPass =  ((board[newx][newy] == null) &&
                (board[newx][newy - direction] != null)&& (board[newx][newy - direction] != this));

        boolean doubleMove = (Math.abs(newy - y) == 2);
        if (super.move(board, NewLocation)){
            FirstMove = false;
            if (TryingToPass) {
                board[newx][newy - direction] = null;
            }
            Vulnerable = doubleMove;
            return true;
        }
        return false;
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();
        if (ValidateMove(this, board, x, y + direction, Moves, false) && FirstMove) {

            ValidateMove(this, board, x, y + direction * 2, Moves, false);
        }

        ValidateMove(this, board, x + 1, y + direction, Moves, true);

        ValidateMove(this, board, x - 1, y + direction, Moves, true);

        if (x + 1 < 8 && board[x + 1][y] != null
                && board[x + 1][y].getClass() == Pawn.class && board[x + 1][y].white != white && ((Pawn) board[x + 1][y]).Vulnerable //Check is piece on right is pawn, and vulnerable
                && (board[x + 1][y + direction] == null)) {  //Check is space moved to is empty
            ValidateMove(this, board, x + 1, y + direction, Moves);
        }

        if (x - 1 > -1 && board[x - 1][y] != null
                && board[x - 1][y].getClass() == Pawn.class && board[x - 1][y].white != white && ((Pawn) board[x - 1][y]).Vulnerable //Check is piece on right is pawn, and vulnerable
                && (board[x - 1][y + direction] == null)) {  //Check is space moved to is empty
            ValidateMove(this, board, x - 1, y + direction, Moves);
        }

        return Moves;
    }
    public boolean ValidateMove(Piece piece, Piece[][] board, int x, int y, List<String> Moves, boolean takeRequired) {
        if ((x > 7 || x < 0 || y > 7 || y < 0)) {
            return false;
        }
        if ((board[x][y] == null) != takeRequired) {    //checks if placement is valid
            return super.ValidateMove(piece, board, x, y, Moves);
        }
        return false;
    }
}

class Knight extends Piece {
    Knight(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();

        //As the knight can jump over pieces, it doesn't need to check if there is anything in the way.
        ValidateMove(this, board, x + 2, y + 1, Moves);

        ValidateMove(this, board, x - 2, y + 1, Moves);

        ValidateMove(this, board, x + 2, y - 1, Moves);

        ValidateMove(this, board, x - 2, y - 1, Moves);

        ValidateMove(this, board, x + 1, y + 2, Moves);

        ValidateMove(this, board, x - 1, y + 2, Moves);

        ValidateMove(this, board, x + 1, y - 2, Moves);

        ValidateMove(this, board, x - 1, y - 2, Moves);

        return Moves;
    }
}

class King extends Piece {
    King(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> GetPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();
        ValidateMove(this, board, x + 1, y - 1, Moves);
        ValidateMove(this, board, x + 1, y, Moves);
        ValidateMove(this, board, x + 1, y + 1, Moves);

        ValidateMove(this, board, x, y - 1, Moves);
        //this would be the kings current location, redundant
        ValidateMove(this, board, x, y + 1, Moves);

        ValidateMove(this, board, x - 1, y - 1, Moves);
        ValidateMove(this, board, x - 1, y, Moves);
        ValidateMove(this, board, x - 1, y + 1, Moves);
        return Moves;
    }

}
