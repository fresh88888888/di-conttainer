package org.flyweight;

public class ChessPiece {
    private ChessPieceUnit chessPieceUnit;
    private int positionX;
    private int getPositionY;

    public ChessPiece(ChessPieceUnit chessPieceUnit, int positionX, int getPositionY) {
        this.chessPieceUnit = chessPieceUnit;
        this.positionX = positionX;
        this.getPositionY = getPositionY;
    }


}
