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
            default -> throw new IllegalArgumentException("Unknown piece: " + piece);
        }
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

    private char getPieceAt(String sq) {
        int idx = squareIndex(sq);
        long mask = 1L << idx;
        if ((whitePawns & mask) != 0) return 'P';
        if ((whiteKnights & mask) != 0) return 'N';
        if ((whiteBishops & mask) != 0) return 'B';
        if ((whiteRooks & mask) != 0) return 'R';
        if ((whiteQueens & mask) != 0) return 'Q';
        if ((whiteKing & mask) != 0) return 'K';
        return '.';
    }

    public void move(String from, String to) {
        char piece = getPieceAt(from);
        if (piece == '.') {
            System.out.println("No piece at " + from);
            return;
        }

        int idx = squareIndex(from);
        long pos = 1L << idx;
        long moves = calculateMoves(piece, idx);

        int targetIdx = squareIndex(to);
        long targetMask = 1L << targetIdx;

        if ((moves & targetMask) == 0) {
            System.out.println("Invalid move for " + piece + " from " + from + " to " + to);
            return;
        }

        switch (piece) {
            case 'P' -> whitePawns &= ~pos;
            case 'N' -> whiteKnights &= ~pos;
            case 'B' -> whiteBishops &= ~pos;
            case 'R' -> whiteRooks &= ~pos;
            case 'Q' -> whiteQueens &= ~pos;
            case 'K' -> whiteKing &= ~pos;
        }

        if ((whitePawns & targetMask) != 0) whitePawns &= ~targetMask;
        if ((whiteKnights & targetMask) != 0) whiteKnights &= ~targetMask;
        if ((whiteBishops & targetMask) != 0) whiteBishops &= ~targetMask;
        if ((whiteRooks & targetMask) != 0) whiteRooks &= ~targetMask;
        if ((whiteQueens & targetMask) != 0) whiteQueens &= ~targetMask;
        if ((whiteKing & targetMask) != 0) whiteKing &= ~targetMask;

        switch (piece) {
            case 'P' -> whitePawns |= targetMask;
            case 'N' -> whiteKnights |= targetMask;
            case 'B' -> whiteBishops |= targetMask;
            case 'R' -> whiteRooks |= targetMask;
            case 'Q' -> whiteQueens |= targetMask;
            case 'K' -> whiteKing |= targetMask;
        }

        System.out.println(piece + " moved from " + from + " to " + to);
    }

    private long calculateMoves(char piece, int idx) {
        long pos = 1L << idx;
        long moves = 0L;
        switch (piece) {
            case 'P' -> moves = pos << 8;
            case 'N' -> moves = knightMoves(pos);
            case 'B' -> moves = bishopRays(idx);
            case 'R' -> moves = rookRays(idx);
            case 'Q' -> moves = bishopRays(idx) | rookRays(idx);
            case 'K' -> moves = kingMoves(pos);
        }
        return moves;
    }

    private long knightMoves(long pos) {
        long l1 = (pos & ~0x8080808080808080L) << 17;
        long l2 = (pos & ~0xC0C0C0C0C0C0C0C0L) << 10;
        long l3 = (pos & ~0xC0C0C0C0C0C0C0C0L) >>> 6;
        long l4 = (pos & ~0x8080808080808080L) >>> 15;
        long r1 = (pos & ~0x0101010101010101L) << 15;
        long r2 = (pos & ~0x0303030303030303L) << 6;
        long r3 = (pos & ~0x0303030303030303L) >>> 10;
        long r4 = (pos & ~0x0101010101010101L) >>> 17;
        return l1 | l2 | l3 | l4 | r1 | r2 | r3 | r4;
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
        int[] bishopDirs = {9, 7, -7, -9};
        return helper(idx, bishopDirs);
    }

    private long rookRays(int idx) {
        int[] rookDirs = {8, -8, 1, -1};
        return helper(idx, rookDirs);
    }

    private long helper(int idx, int[] dirs) {
        long result = 0L;
        long occupied = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;

        int rank = idx / 8;
        int file = idx % 8;

        for (int dir: dirs) {
            int r = rank;
            int f = file;

            while (true) {
                r += dir / 8;
                f += dir % 8;

                if (r < 0 || r > 7 || f < 0 || f > 7) break;

                int sq = r * 8 + f;
                result |= 1L << sq;

                if ((occupied & (1L << sq)) != 0) break;
            }
        }
        return result;
    }

    public void pieceSees(char piece, String square) {
        int idx = squareIndex(square);
        long pos = 1L << idx;
        long moves = calculateMoves(piece, idx);

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
}