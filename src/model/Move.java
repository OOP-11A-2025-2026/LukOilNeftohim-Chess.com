package model;
public class Move{
    public static final byte FLAG_SHORT_CASTLE=  0b00000001;
    public static final byte FLAG_LONG_CASTLE =  0b00000010;
    public static final byte FLAG_EN_PASSANT  =  0b00000100;
    public static final byte FLAG_PROMOTION   =  0b00001000;
    public static final byte FLAG_CHECK       =  0b00010000;
    public static final byte FLAG_MATE        =  0b00100000;

    public Piece piece;         
    public byte target;
    public byte flags;
    public byte disambiguation; 
    
    public Move() {};
}