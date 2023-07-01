package org.flyweight;

import java.util.HashMap;
import java.util.Map;

public class ChessBoard {
    private final Map<Integer, ChessPiece> chessPieces = new HashMap<>();

    public ChessBoard() {

    }

    public void init(){
        chessPieces.put(1, new ChessPiece(ChessPieceUnitFactory.getChessPiece(1), 0, 0));
        chessPieces.put(2, new ChessPiece(ChessPieceUnitFactory.getChessPiece(2), 1,1));
        //...省略其他棋子的代码...
    }

    public void move(int chessPieceId, int toPositionX, int toPositionY){
        //...省略...
    }
}
