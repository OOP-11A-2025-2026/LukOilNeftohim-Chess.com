package model;
public class Move{
    public static final byte FLAG_SHORT_CASTLE=  0b00000001;
    public static final byte FLAG_LONG_CASTLE =  0b00000010;
    public static final byte FLAG_EN_PASSANT  =  0b00000100;
    public static final byte FLAG_PROMOTION   =  0b00001000;
    public static final byte FLAG_CAPTURE     =  0b00010000;
    public static final byte FLAG_CHECK       =  0b00100000;
    public static final byte FLAG_MATE        =  0b01000000;

    public Piece piece;         
    public int target;
    public int flags;
    public int disambiguation; 
    
    public Move() {};

    @Override
    public String toString() {

        if ((flags & FLAG_SHORT_CASTLE) != 0) return "O-O";
        if ((flags & FLAG_LONG_CASTLE) != 0)  return "O-O-O";

        StringBuilder sb = new StringBuilder();

        if (piece.type() != Type.PAWN) {
            sb.append(piece.toString().charAt(0));
        }

        if ((flags & FLAG_CAPTURE) != 0) {
            if (piece.type() == Type.PAWN) {
                sb.append((char)('a' + (disambiguation % 8)));
            }
            sb.append("x");
        }

        sb.append(indexToSquare(target));

        if ((flags & FLAG_PROMOTION) != 0) {
            sb.append("=");
            sb.append(piece.toString().charAt(0));
        }

        if ((flags & FLAG_CHECK) != 0) sb.append("+");
        if ((flags & FLAG_MATE) != 0)  sb.append("#");

        return sb.toString();
    }

    private String indexToSquare(int index) {
        int file = index % 8;
        int rank = index / 8;
        return "" + (char)('a' + file) + (rank + 1);
    }
}