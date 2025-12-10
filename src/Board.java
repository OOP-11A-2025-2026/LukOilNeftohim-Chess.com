public class Board {

    private long whitePawns = 0L;
    private long whiteKnights = 0L;
    private long whiteBishops = 0L;
    private long whiteRooks = 0L;
    private long whiteQueens = 0L;
    private long whiteKing = 0L;

    public static int squareIndex(String sq) {
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        return (rank - '1') * 8 + (file - 'a');
    }

    public void set(String command) {
        char piece = command.charAt(0);
        String square = command.substring(1);

        int idx = squareIndex(square);
        long mask = 1L << idx;

        switch (piece) {
            case 'P' -> whitePawns |= mask;
            case 'N' -> whiteKnights |= mask;
            case 'B' -> whiteBishops |= mask;
            case 'R' -> whiteRooks |= mask;
            case 'Q' -> whiteQueens |= mask;
            case 'K' -> whiteKing |= mask;
            default  -> throw new IllegalArgumentException("Unknown piece: " + piece);
        }
    }

    private int pieceValue(char piece) {
        return switch (piece) {
            case 'P'      -> 1;
            case 'N', 'B' -> 3;
            case 'R'      -> 5;
            case 'Q'      -> 9;
            case 'K'      -> 1000;
            default       -> 0;
        };
    }

    public int material() {
        return Long.bitCount(whitePawns) * 1 +
                Long.bitCount(whiteKnights) * 3 +
                Long.bitCount(whiteBishops) * 3 +
                Long.bitCount(whiteRooks) * 5 +
                Long.bitCount(whiteQueens) * 9 +
                Long.bitCount(whiteKing) * 0;
    }

    public void print() {
        System.out.println("  a b c d e f g h");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int idx = rank * 8 + file;
                long mask = 1L << idx;
                char c = '.';

                if ((whitePawns & mask) != 0) c = 'P';
                else if ((whiteKnights & mask) != 0) c = 'N';
                else if ((whiteBishops & mask) != 0) c = 'B';
                else if ((whiteRooks & mask) != 0) c = 'R';
                else if ((whiteQueens & mask) != 0) c = 'Q';
                else if ((whiteKing & mask) != 0) c = 'K';

                System.out.print(c + " ");
            }
            System.out.println(" " + (rank + 1));
        }

        System.out.println("  a b c d e f g h");
    }

    public boolean isOccupied(String sq) {
        int idx = squareIndex(sq);
        long mask = 1L << idx;
        return ((whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing) & mask) != 0;
    }

    public void pieceSees(char piece, String square) {
        int idx = squareIndex(square);
        long pos = 1L << idx;

        long moves = 0L;

        switch (piece) {
            case 'P':
                moves = pos << 8;
                break;
            case 'N':
                long l1 = (pos & ~0x8080808080808080L) << 17;
                long l2 = (pos & ~0xC0C0C0C0C0C0C0C0L) << 10;
                long l3 = (pos & ~0xC0C0C0C0C0C0C0C0L) >>> 6;
                long l4 = (pos & ~0x8080808080808080L) >>> 15;
                long r1 = (pos & ~0x0101010101010101L) << 15;
                long r2 = (pos & ~0x0303030303030303L) << 6;
                long r3 = (pos & ~0x0303030303030303L) >>> 10;
                long r4 = (pos & ~0x0101010101010101L) >>> 17;
                moves = l1 | l2 | l3 | l4 | r1 | r2 | r3 | r4;
                break;
            case 'B':
                moves = bishopRays(idx);
                break;
            case 'R':
                moves = rookRays(idx);
                break;
            case 'Q':
                moves = bishopRays(idx) | rookRays(idx);
                break;
            case 'K':
                moves = kingMoves(pos);
                break;
        }

        System.out.println("  a b c d e f g h");
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                long mask = 1L << sq;
                if ((pos & mask) != 0) System.out.print(piece + " ");
                else if ((moves & mask) != 0) System.out.print("X ");
                else System.out.print(". ");
            }
            System.out.println(" " + (rank + 1));
        }
        System.out.println("  a b c d e f g h");
    }

    private long kingMoves(long pos) {
        long notA = 0xfefefefefefefefeL;
        long notH = 0x7f7f7f7f7f7f7f7fL;
        long moves = 0L;
        moves |= (pos & notA) >>> 1;
        moves |= (pos & notH) << 1;
        moves |= pos << 8;
        moves |= pos >>> 8;
        moves |= (pos & notA) << 7;
        moves |= (pos & notH) << 9;
        moves |= (pos & notA) >>> 9;
        moves |= (pos & notH) >>> 7;
        return moves;
    }

    private long bishopRays(int idx) {
        long result = 0L;
        int rank = idx / 8;
        int file = idx % 8;

        for (int r = rank+1, f = file+1; r<8 && f<8; r++, f++) result |= 1L << (r*8+f);
        for (int r = rank+1, f = file-1; r<8 && f>=0; r++, f--) result |= 1L << (r*8+f);
        for (int r = rank-1, f = file+1; r>=0 && f<8; r--, f++) result |= 1L << (r*8+f);
        for (int r = rank-1, f = file-1; r>=0 && f>=0; r--, f--) result |= 1L << (r*8+f);

        return result;
    }

    private long rookRays(int idx) {
        long result = 0L;
        int rank = idx / 8;
        int file = idx % 8;

        for (int r=rank+1; r<8; r++) result |= 1L << (r*8+file);
        for (int r=rank-1; r>=0; r--) result |= 1L << (r*8+file);
        for (int f=file+1; f<8; f++) result |= 1L << (rank*8+f);
        for (int f=file-1; f>=0; f--) result |= 1L << (rank*8+f);

        return result;
    }
}