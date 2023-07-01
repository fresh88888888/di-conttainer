package org.flyweight;

import java.util.HashMap;
import java.util.Map;

public class ChessPieceUnitFactory {
    private static final Map<Integer, ChessPieceUnit> pieces = new HashMap<>();

    static {
        pieces.put(1, new ChessPieceUnit(1, "车", ChessPieceUnit.Color.RED));
        pieces.put(2, new ChessPieceUnit(2, "炮", ChessPieceUnit.Color.BLACK));
    }

    public static ChessPieceUnit getChessPiece(int chessPieceId){
       return pieces.get(chessPieceId);
    }
}
