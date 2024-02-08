package com.starlight.chessTCP;

import java.util.ArrayList;
import java.util.Collections;
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

        return new Pawn(type,x,y,white);
    }

    public static boolean ValidateMove(Piece piece, Piece[][] board, int x, int y, List<String> Moves) {
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
        {   //right
            for (int i = 1; i < 8; i++) { //Up in both axis
                if(piece.x + i > 7 || piece.y + i > 7){break;}  //Feels very illegal, gatekeeping

                Piece temp = board[piece.x + i][piece.y + i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x + i) + "" + (piece.y + i)); //can move here

                if (temp != null) {
                    break;      //opposite colour, take
                }
            }
            for (int i = 1; i < 8; i++) {    //up x , down Y
                if(piece.x + i > 7 || piece.y - i < 0) {break;}  //Feels very illegal, gatekeeping

                Piece temp = board[piece.x + i][piece.y - i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x + i) + "" + (piece.y - i)); //can move here, its an empty space

                if (temp != null) {
                    break;      //The piece here can be taken, but must stop here.
                }
            }
        }

        {   //left
            for (int i = 1; i < 8; i++) { //Up in both axis
                if(piece.x - i < 0 || piece.y + i > 7){break;}  //Feels very illegal, gatekeeping

                Piece temp = board[piece.x - i][piece.y + i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x - i) + "" + (piece.y + i)); //can move here

                if (temp != null) {
                    break;      //opposite colour, take
                }
            }
            for (int i = 1; i < 8; i++) {    //up x , down Y
                if(piece.x - i < 0|| piece.y - i < 0){break;}  //Feels very illegal, gatekeeping
                Piece temp = board[piece.x - i][piece.y - i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x - i) + "" + (piece.y - i)); //can move here, its an empty space

                if (temp != null) {
                    break;      //The piece here can be taken, but must stop here.
                }
            }

        }
        return moves;
    }
}

interface Straight{
    static List<String> GetPossibleMoves(Piece piece, Piece[][] board){
        List<String> moves = new ArrayList<>(); //return val
        {   //horizontal
            for (int i = piece.x + 1; i < 8; i++) { //To the right of the board

                Piece temp = board[i][piece.y];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((i) + "" + piece.y); //can move here

                if (temp != null) {
                    break;      //opposite colour, take
                }
            }
            for (int i = piece.x - 1; i > -1; i--) {    //To the left of the board

                Piece temp = board[i][piece.y];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((i) + "" + piece.y); //can move here, its an empty space

                if (temp != null) {
                    break;      //The piece here can be taken, but must stop here.
                }
            }
        }

        {   //vertical
            for (int i = piece.y + 1; i < 8; i++) { //To the right of the board

                Piece temp = board[piece.x][i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x) + "" + i); //can move here

                if (temp != null) {
                    break;      //opposite colour, take
                }
            }
            for (int i = piece.y - 1; i > -1; i--) {    //To the left of the board

                Piece temp = board[piece.x][i];

                if (temp != null && temp.white == piece.white) {
                    break;      //The piece here is the same colour, cant move.
                }

                moves.add((piece.x) + "" + i); //can move here, its an empty space

                if (temp != null) {
                    break;      //The piece here can be taken, but must stop here.
                }
            }

        }
        return moves;
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
        if (ValidateMove(this, board, x, y+ direction, Moves)) {
            Moves.add(x + "" + (y + direction));

        }
        //over stuffed can probs simplify
        if (FirstMove && board[x][y+ direction] == null &&
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

