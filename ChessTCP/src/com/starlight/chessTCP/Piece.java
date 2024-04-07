package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;

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

    public boolean move(Piece[][] board, String newLocation) {
        if (!getLegalMoves(board).contains(newLocation)) {return false;}
            int oldX = x;
            int oldY = y;   //Saves the pieces current location before move

            x = Character.getNumericValue(newLocation.charAt(0));
            y = Character.getNumericValue(newLocation.charAt(1));   //Pieces new location from string

            board[x][y] = this; //Places this piece at the new location
            board[oldX][oldY] = null;   //Sets the previous location to empty
            return true;

    }

    public abstract List<String> getPossibleMoves(Piece[][] board); //Gets all moves it could make with minimal legality checks

    public List<String> getLegalMoves(Piece[][] board) {    //Checks all possible moves for legality
        King k = null;

        for (Piece[] row :          //Loops through all pieces to find king
                board) {
            for (Piece p :
                    row) {
                if (p != null && p.white == white && p.getClass() == King.class) {
                    k = (King) p;
                    break;
                }
            }
            if (k != null) {
                break;
            }
        }

        if (k == null) {return null;}       //If a player doesn't have a king, its game over they cant move

        List<String> legalMoves = new ArrayList<>();    //return val

        for (String Move :  //Loops through all possible moves and checks legality
                this.getPossibleMoves(board)) {
            if (simulateMove(board, this,   //Checks if king is safe after this move is made. If it isn't, move is not legal
                    Character.getNumericValue(Move.charAt(0)), Character.getNumericValue(Move.charAt(1)), k)) {
                legalMoves.add(Move);
            }
        }

        return legalMoves;
    }   //returns possible moves after legality check

    public static Piece createPiece(char type, int x, int y, boolean white) {

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
    }   //Simple factory function

    public boolean validateMove(Piece piece, Piece[][] board, int x, int y, List<String> moves) {
        //gate-keeping
        if ((x > 7 || x < 0 || y > 7 || y < 0)) {return false;  }      //Checks the location is out of range
        if (board[x][y] != null && board[x][y].white == piece.white) {return false;}  //If a piece in location is same colour, cant move there


        moves.add(x + String.valueOf(y));
        return true;

    }   //checks valid

    static boolean isLocationSafe(Piece[][] board, Piece piece, int x, int y) {
        for (Piece[] col :
                board) {
            for (Piece p :
                    col) {  //Iterates through all pieces
                if (p != null && piece.white != p.white &&
                        p.getPossibleMoves(board).contains(x + String.valueOf(y))) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean simulateMove(Piece[][] board, Piece piece, int x, int y, King king) {
        int defaultX = piece.x;
        int defaultY = piece.y; //Use to return the piece to its start location after checks

        //HACK?
        Piece temporaryStorage = board[x][y]; //Gets the piece at the testing location if this move is a capture

        board[defaultX][defaultY] = null;     //sets the current space to empty
        board[x][y] = piece;       //Sets the piece to the new location

        piece.x = x;
        piece.y = y;     //Ignores legality checks and forces the move

        // check if the king is safe
        boolean locationSafe = isLocationSafe(board, king, king.x, king.y);

        //resets the locations of test piece and potential capture
        board[defaultX][defaultY] = piece;
        board[x][y] = temporaryStorage;

        piece.x = defaultX;
        piece.y = defaultY;
        return locationSafe;
    }
}

interface Diagonal {
    static List<String> getPossibleMoves(Piece piece, Piece[][] board) {

        List<String> moves = new ArrayList<>(); //return val
        int testX;
        int testY;
        int distance = 0;
        {   //right

            do {
                distance++;
                testX = piece.x + distance;
                testY = piece.y + distance;
            } while (checkEndOfPath(piece, board, testX, testY, moves));


            distance = 0;
            do {
                distance++;
                testX = piece.x + distance;
                testY = piece.y - distance;
            } while (checkEndOfPath(piece, board, testX, testY, moves));
        }

        {   //left
            distance = 0;
            do {
                distance++;
                testX = piece.x - distance;
                testY = piece.y + distance;
            } while (checkEndOfPath(piece, board, testX, testY, moves));


            distance = 0;
            do {
                distance++;
                testX = piece.x - distance;
                testY = piece.y - distance;
            } while (checkEndOfPath(piece, board, testX, testY, moves));

        }
        return moves;
    }

    private static boolean checkEndOfPath(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        return piece.validateMove(piece, board, x, y, Moves) &&
                (board[x][y] == null);  //If it gets here, move is valid. If space is not null, a piece is being taken so return false
    }
}

interface Straight {
    static List<String> getPossibleMoves(Piece piece, Piece[][] board) {

        List<String> moves = new ArrayList<>(); //return val
        int testX;
        int testY;
        int distance = 0;

        do {
            distance++;
            testX = piece.x + distance;
            testY = piece.y;
        } while (checkEndOfPath(piece, board, testX, testY, moves));


        distance = 0;
        do {
            distance++;
            testX = piece.x - distance;
            testY = piece.y;
        } while (checkEndOfPath(piece, board, testX, testY, moves));


        distance = 0;
        do {
            distance++;
            testX = piece.x;
            testY = piece.y + distance;
        } while (checkEndOfPath(piece, board, testX, testY, moves));


        distance = 0;
        do {
            distance++;
            testX = piece.x;
            testY = piece.y - distance;
        } while (checkEndOfPath(piece, board, testX, testY, moves));

        return moves;
    }

    private static boolean checkEndOfPath(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
        return piece.validateMove(piece, board, x, y, Moves) &&
                (board[x][y] == null);  //If it gets here, move is valid. If space is not null, a piece is being taken so return false
    }
}

class Rook extends Piece implements Straight {

    public boolean firstMove = true;

    Rook(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public boolean move(Piece[][] board, String newLocation) {
        if (super.move(board, newLocation)) {
            firstMove = false;
            return true;
        }
        return false;
    }

    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        return Straight.getPossibleMoves(this, board);
    }
}

class Bishop extends Piece implements Diagonal {

    Bishop(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        return Diagonal.getPossibleMoves(this, board);
    }
}

class Queen extends Piece implements Straight, Diagonal {


    Queen(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        List<String> moves = Straight.getPossibleMoves(this, board);
        moves.addAll(Diagonal.getPossibleMoves(this, board));
        return moves;
    }

}

class Pawn extends Piece {

    private boolean firstMove = true;
    public boolean vulnerable = false;      //If could be captured via en passant
    public int direction;

    Pawn(char type, int x, int y, boolean white) {
        super(type, x, y, white);
        direction = (white ? -1 : 1);
    }

    @Override
    public boolean move(Piece[][] board, String newLocation) {

        int newX = Character.getNumericValue(newLocation.charAt(0));
        int newY = Character.getNumericValue(newLocation.charAt(1));

        //I don't need to use too many checks here, as the only chance to move into an empty space with a piece behind it is en-passant
        boolean attemptingEnPassant = ((board[newX][newY] == null) &&
                (board[newX][newY - direction] != null) && (board[newX][newY - direction] != this));

        boolean doubleMove = (Math.abs(newY - y) == 2);
        if (!super.move(board, newLocation)) {return false;}     //The move failed so no need to check anymore

        firstMove = false;
        if (attemptingEnPassant) {
            board[newX][newY - direction] = null;
        }
        vulnerable = doubleMove;

        return true;
    }
    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        List<String> moves = new ArrayList<>();
        if (ValidateMove(this, board, x, y + direction, moves, false) && firstMove) {
            ValidateMove(this, board, x, y + direction * 2, moves, false);      //If it can move forward one, we know that space is empty
        }
        //Diagonal moves (capture required)
        ValidateMove(this, board, x + 1, y + direction, moves, true);
        ValidateMove(this, board, x - 1, y + direction, moves, true);


        //en passant
        if (x + 1 < 8 && board[x + 1][y] != null
                && board[x + 1][y].getClass() == Pawn.class && board[x + 1][y].white != white && ((Pawn) board[x + 1][y]).vulnerable //Check is piece on right is pawn, and vulnerable
                && (board[x + 1][y + direction] == null)) {  //Check is space moved to is empty
            validateMove(this, board, x + 1, y + direction, moves);
        }

        if (x - 1 > -1 && board[x - 1][y] != null
                && board[x - 1][y].getClass() == Pawn.class && board[x - 1][y].white != white && ((Pawn) board[x - 1][y]).vulnerable //Check is piece on right is pawn, and vulnerable
                && (board[x - 1][y + direction] == null)) {  //Check is space moved to is empty
            validateMove(this, board, x - 1, y + direction, moves);
        }

        return moves;
    }
    public boolean ValidateMove(Piece piece, Piece[][] board, int x, int y, List<String> Moves, boolean takeRequired) { //Pawn specific conditions
        if ((x > 7 || x < 0 || y > 7 || y < 0)) {
            return false;
        }
        if ((board[x][y] == null) != takeRequired) {    //checks if placement is valid
            return super.validateMove(piece, board, x, y, Moves);
        }
        return false;
    }
}

class Knight extends Piece {
    Knight(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();

        //As the knight can jump over pieces, it doesn't need to check if there is anything in the way.
        validateMove(this, board, x + 2, y + 1, Moves);

        validateMove(this, board, x - 2, y + 1, Moves);

        validateMove(this, board, x + 2, y - 1, Moves);

        validateMove(this, board, x - 2, y - 1, Moves);

        validateMove(this, board, x + 1, y + 2, Moves);

        validateMove(this, board, x - 1, y + 2, Moves);

        validateMove(this, board, x + 1, y - 2, Moves);

        validateMove(this, board, x - 1, y - 2, Moves);

        return Moves;
    }
}

class King extends Piece {

    private boolean firstMove = true;

    King(char type, int x, int y, boolean white) {
        super(type, x, y, white);
    }

    @Override
    public boolean move(Piece[][] board, String newLocation) {
        int StartX = this.x;        //Saves X to check later
        int newX = Character.getNumericValue(newLocation.charAt(0));
        boolean AttemptingCastle = (abs(StartX - newX) == 2);       //If this piece is moving 2 spaces, its castling


        if (!super.move(board, newLocation)) {return false;} ///Gate-keeping
        firstMove = false;
        if (!AttemptingCastle) { return true; }

        int RookDirection = ((StartX - newX) > 0) ? -1 : 1;  //Finds which side the rook is on

        //You know there is going to be a rook or this move wouldn't have been possible
        while (board[x + RookDirection][y] == null) {  //scans that direction for the rook
            RookDirection += RookDirection;
        }

        Rook rook = (Rook) board[x + RookDirection][y];   //Get the rook


        //Have to force the move as the king is now in the way, so it doesn't register as possible
        board[rook.x][y] = null;
        rook.x = this.x - (((StartX - newX) > 0) ? -1 : 1);  //Place the rook on the other side of the king
        board[rook.x][y] = rook;


        return true;
    }

    @Override
    public List<String> getPossibleMoves(Piece[][] board) {
        List<String> Moves = new ArrayList<>();
        validateMove(this, board, x + 1, y - 1, Moves);
        validateMove(this, board, x + 1, y, Moves);
        validateMove(this, board, x + 1, y + 1, Moves);

        validateMove(this, board, x, y - 1, Moves);
        //this would be the kings current location, redundant
        validateMove(this, board, x, y + 1, Moves);

        validateMove(this, board, x - 1, y - 1, Moves);
        validateMove(this, board, x - 1, y, Moves);
        validateMove(this, board, x - 1, y + 1, Moves);

        //Castle(board, Moves);

        return Moves;
    }

    @Override
    public List<String> getLegalMoves(Piece[][] board) {
        List<String> Moves = super.getLegalMoves(board);
        Castle(board, Moves);
        return Moves;
    }

    private boolean Castle(Piece[][] board, List<String> moves) {
        boolean added = false;
        if (!firstMove) {return false;} //Can only do this on first move
        if (x + 3 < 8 &&   //checks in any passed space is in check
                isLocationSafe(board, this, x, y) &&
                (x + 1) < 8 && isLocationSafe(board, this, x + 1, y) && board[x + 1][y] == null &&
                (x + 2) < 8 && isLocationSafe(board, this, x + 2, y) && board[x + 2][y] == null) {

            int counter = 3;    //Starts the counter at 3, as the rook must be at-least that far away
            boolean valid = false;

            while (counter + x < 8) {
                if (board[x + counter][y] != null) {    //if there is a piece here, if it's not a rook this check fails
                    valid = (board[x + counter][y].white == white && //Is piece same colour
                            board[x + counter][y].getClass() == Rook.class &&
                            ((Rook)board[x + counter][y]).firstMove);  //is Piece a rook
                    break;
                }
                counter++;
            }
            if (valid) {
                moves.add((x + 2) + String.valueOf(y));    //adds the move to the list
                added = true;
            }
        }

        if (x - 3 < 8 &&   //checks in any passed space is in check
                isLocationSafe(board, this, x, y) &&
                (x - 1) > -1 && isLocationSafe(board, this, x - 1, y) && board[x - 1][y] == null &&
                (x - 2) > -1 && isLocationSafe(board, this, x - 2, y) && board[x - 2][y] == null) {

            int counter = 3;    //Starts the counter at 3, as the rook must be atleast that far away
            boolean valid = false;

            while (x- counter > -1) {   //Keeps the checks inside range
                if (board[x - counter][y] != null) {
                    valid = (board[x - counter][y].white == white && //Is piece same colour
                            board[x - counter][y].getClass() == Rook.class &&//is Piece a rook
                            ((Rook)board[x - counter][y]).firstMove);
                    break;
                }
                counter++;
            }
            if (valid) {
                moves.add((x - 2) + String.valueOf(y));
                added = true;
            }
        }
        return added;
    }


}
