public class Board{
  /* =========================
       Bitboards
       ========================= */

    /**
     * Bitboards:
     * 0..5  -> WHITE  (PAWN..KING)
     * 6..11 -> BLACK  (PAWN..KING)
     */
    private final long[] bb = new long[12];

    Color sideToMove = Color.WHITE;

    /* =========================
       Construction
       ========================= */

    public Board() {
        setupInitialPosition();
    }

    private void setupInitialPosition() {
        // Pawns
        bb[idx(Type.PAWN, Color.WHITE)] = 0x000000000000FF00L;
        bb[idx(Type.PAWN, Color.BLACK)] = 0x00FF000000000000L;

        // Rooks
        bb[idx(Type.ROOK, Color.WHITE)] = 0x0000000000000081L;
        bb[idx(Type.ROOK, Color.BLACK)] = 0x8100000000000000L;

        // Knights
        bb[idx(Type.KNIGHT, Color.WHITE)] = 0x0000000000000042L;
        bb[idx(Type.KNIGHT, Color.BLACK)] = 0x4200000000000000L;

        // Bishops
        bb[idx(Type.BISHOP, Color.WHITE)] = 0x0000000000000024L;
        bb[idx(Type.BISHOP, Color.BLACK)] = 0x2400000000000000L;

        // Queens
        bb[idx(Type.QUEEN, Color.WHITE)] = 0x0000000000000008L;
        bb[idx(Type.QUEEN, Color.BLACK)] = 0x0800000000000000L;

        // Kings
        bb[idx(Type.KING, Color.WHITE)] = 0x0000000000000010L;
        bb[idx(Type.KING, Color.BLACK)] = 0x1000000000000000L;
    }

    /* =========================
       Index helpers
       ========================= */

       /**
 * Convert algebraic square notation (e.g. "e2") to 0..63 index.
 * @param sq the square in algebraic notation
 * @return index 0..63
 * @throws IllegalArgumentException if input is invalid
 */
public int sqIdx(String sq) {
    if (sq == null || sq.length() != 2) 
        throw new IllegalArgumentException("Invalid square: " + sq);
    
    char file = sq.charAt(0);
    char rank = sq.charAt(1);

    if (file < 'a' || file > 'h' || rank < '1' || rank > '8') 
        throw new IllegalArgumentException("Square out of bounds: " + sq);
    

    return (rank - '1') * 8 + (file - 'a');
}

    private static int idx(Type t, Color c) { return t.ordinal() + (c == Color.WHITE ? 0 : 6); }



    private static String squareName(int sq) {
        char file = (char) ('a' + (sq % 8));
        char rank = (char) ('1' + (sq / 8));
        return "" + file + rank;
    }

    /* =========================
       Occupancy
       ========================= */

    private long whiteOcc() {
        long o = 0L;
        for (int i = 0; i < 6; i++) o |= bb[i];
        return o;
    }

    private long blackOcc() {
        long o = 0L;
        for (int i = 6; i < 12; i++) o |= bb[i];
        return o;
    }

    private long allOcc() {
        return whiteOcc() | blackOcc();
    }

    /* =========================
       Piece access
       ========================= */

    /**
     * @return index in bb[] or -1 if empty
     */
    private int pieceIndexAt(int square) {
        long mask = 1L << square;
        for (int i = 0; i < 12; i++) {
            if ((bb[i] & mask) != 0) return i;
        }
        return -1;
    }

public Piece getPieceAt(String sq) {
    int idx = sqIdx(sq); 
    return getPieceAt(idx);     
}

public Piece getPieceAt(int idx) {
    if (idx < 0 || idx > 63) return null;
    int pieceIdx = pieceIndexAt(idx);
    if (pieceIdx == -1) return null;
    Type type = Type.values()[pieceIdx % 6];
    Color color = pieceIdx < 6 ? Color.WHITE : Color.BLACK;
    return new Piece(type, color);
}



    public void move(String from, String to) {
        int fromSq = sqIdx(from);
        int toSq   = sqIdx(to);

        int moverIdx = pieceIndexAt(fromSq);
        if (moverIdx == -1) {
            throw new IllegalStateException("No piece at " + from);
        }

        Color moverColor = moverIdx < 6 ? Color.WHITE : Color.BLACK;
        if (moverColor != sideToMove) {
            throw new IllegalStateException("Wrong side to move");
        }

        int capturedIdx = pieceIndexAt(toSq);

        // remove mover from source
        bb[moverIdx] &= ~(1L << fromSq);

        // capture
        if (capturedIdx != -1) {
            bb[capturedIdx] &= ~(1L << toSq);
        }

        // place mover on target
        bb[moverIdx] |= 1L << toSq;

        sideToMove = sideToMove.opposite();
    }

    /* =========================
       Debug / display
       ========================= */

    public void print() {
        for (int r = 7; r >= 0; r--) {
            for (int f = 0; f < 8; f++) {
                int sq = r * 8 + f;
                int idx = pieceIndexAt(sq);
                if (idx == -1) {
                    System.out.print(". ");
                } else {
                    Type t = Type.values()[idx % 6];
                    Color c = idx < 6 ? Color.WHITE : Color.BLACK;
                    char ch = t.toSANChar();
                    if (ch == '\0') ch = 'P';
                    if (c == Color.BLACK) ch = Character.toLowerCase(ch);
                    System.out.print(ch + " ");
                }
            }
            System.out.println();
        }
        System.out.println("Side to move: " + sideToMove);
        System.out.println();
    }

    public static void main(String[] args) {
        Board b = new Board();
        b.print();

        b.move("e2", "e4");
        b.print();

        b.move("e7", "e5");
        b.print();
    }
}
