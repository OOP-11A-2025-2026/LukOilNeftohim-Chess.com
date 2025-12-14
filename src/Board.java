public class Board {

    private long whitePawns = 0L;
    private long whiteKnights = 0L;
    private long whiteBishops = 0L;
    private long whiteRooks = 0L;
    private long whiteQueens = 0L;
    private long whiteKing = 0L;

    private long blackPawns = 0L;
    private long blackKnights = 0L;
    private long blackBishops = 0L;
    private long blackRooks = 0L;
    private long blackQueens = 0L;
    private long blackKing = 0L;

    boolean whiteToMove = true;

    public static int squareIndex(String sq) {
        if (sq == null || sq.length() < 2) throw new IllegalArgumentException("Invalid square: " + sq);
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        return (rank - '1') * 8 + (file - 'a');
    }

    public void set(String command) {
        if (command == null || command.length() < 2) throw new IllegalArgumentException("Invalid command: " + command);
        char piece = command.charAt(0);
        String square = command.substring(1);

        int idx = squareIndex(square);
        long mask = 1L << idx;

        switch (piece) {
            case 'P' -> {
                if (whiteToMove) whitePawns |= mask;
                else blackPawns |= mask;
            }
            case 'N' -> {
                if (whiteToMove) whiteKnights |= mask;
                else blackKnights |= mask;
            }
            case 'B' -> {
                if (whiteToMove) whiteBishops |= mask;
                else blackBishops |= mask;
            }
            case 'R' -> {
                if (whiteToMove) whiteRooks |= mask;
                else blackRooks |= mask;
            }
            case 'Q' -> {
                if (whiteToMove) whiteQueens |= mask;
                else blackQueens |= mask;
            }
            case 'K' -> {
                if (whiteToMove) whiteKing |= mask;
                else blackKing |= mask;
            }
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
                char c = getPieceChar(mask);
                System.out.print(c + " ");
            }
            System.out.println(" " + (rank + 1));
        }
        System.out.println("  a b c d e f g h");
        System.out.println("Side to move: " + (whiteToMove ? "White" : "Black"));
    }

    private long WhiteOcc() {
        return whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
    }

    private long BlackOcc() {
        return blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
    }

    private long AllOcc() {
        return WhiteOcc() | BlackOcc();
    }

    private char getPieceChar(long mask) {
        if ((whitePawns & mask) != 0)    return 'P';
        if ((whiteKnights & mask) != 0)  return 'N';
        if ((whiteBishops & mask) != 0)  return 'B';
        if ((whiteRooks & mask) != 0)    return 'R';
        if ((whiteQueens & mask) != 0)   return 'Q';
        if ((whiteKing & mask) != 0)     return 'K';
        if ((blackPawns & mask) != 0)    return 'p';
        if ((blackKnights & mask) != 0)  return 'n';
        if ((blackBishops & mask) != 0)  return 'b';
        if ((blackRooks & mask) != 0)    return 'r';
        if ((blackQueens & mask) != 0)   return 'q';
        if ((blackKing & mask) != 0)     return 'k';
        return '.';
    }

    private char getPieceAt(String sq) {
        int idx = squareIndex(sq);
        long mask = 1L << idx;
        return getPieceChar(mask);
    }

    public void move(String from, String to) {
        char piece = getPieceAt(from);
        if (piece == '.') {
            System.out.println("No piece at " + from);
            return;
        }

        boolean pieceIsWhite = Character.isUpperCase(piece);
        if (pieceIsWhite != whiteToMove) {
            System.out.println("It's " + (whiteToMove ? "White" : "Black") + "'s turn: cannot move " + piece);
            return;
        }
        int idx = squareIndex(from);
        long pos = 1L << idx;
        long moves = calculateMoves(piece, idx);

        int targetIdx = squareIndex(to);
        long targetMask = 1L << targetIdx;

        long friendlyPiece = pieceIsWhite ? WhiteOcc() : BlackOcc();

        if ((moves & targetMask) == 0) {
            System.out.println("Invalid move for " + piece + " from " + from + " to " + to);
            return;
        }

        if ((friendlyPiece & targetMask) != 0) {
            System.out.println("Cannot capture your own piece");
            return;
        }

        removePiece(piece, pos);
        char targetPiece = getPieceChar(targetMask);
        if (targetPiece != '.') {
            removePiece(pieceIsWhite ? Character.toLowerCase(getPieceAt(to)) : Character.toUpperCase(getPieceAt(to)), targetMask);
        }

        putPiece(piece, targetMask);

        whiteToMove = !whiteToMove;
        System.out.println(piece + " moved from " + from + " to " + to);
    }

    private void removePiece(char piece, long mask) {
        switch (piece) {
            case 'P' -> whitePawns &= ~mask;
            case 'N' -> whiteKnights &= ~mask;
            case 'B' -> whiteBishops &= ~mask;
            case 'R' -> whiteRooks &= ~mask;
            case 'Q' -> whiteQueens &= ~mask;
            case 'K' -> whiteKing &= ~mask;
            case 'p' -> blackPawns &= ~mask;
            case 'n' -> blackKnights &= ~mask;
            case 'b' -> blackBishops &= ~mask;
            case 'r' -> blackRooks &= ~mask;
            case 'q' -> blackQueens &= ~mask;
            case 'k' -> blackKing &= ~mask;
            default -> throw new IllegalArgumentException("Invalid piece " + piece);
        }
    }

    private void putPiece(char piece, long mask) {
        switch (piece) {
            case 'P' -> whitePawns |= mask;
            case 'N' -> whiteKnights |= mask;
            case 'B' -> whiteBishops |= mask;
            case 'R' -> whiteRooks |= mask;
            case 'Q' -> whiteQueens |= mask;
            case 'K' -> whiteKing |= mask;
            case 'p' -> blackPawns |= mask;
            case 'n' -> blackKnights |= mask;
            case 'b' -> blackBishops |= mask;
            case 'r' -> blackRooks |= mask;
            case 'q' -> blackQueens |= mask;
            case 'k' -> blackKing |= mask;
            default -> throw new IllegalArgumentException("Invalid piece " + piece);
        }
    }

    private long calculateMoves(char piece, int idx) {
        long pos = 1L << idx;
        long moves = 0L;
        boolean pieceIsWhite = Character.isUpperCase(piece);
        long friendlyPieces = pieceIsWhite ? WhiteOcc() : BlackOcc();
        long enemyPieces = pieceIsWhite ? BlackOcc() : WhiteOcc();
        long occupied = AllOcc();

        switch (Character.toLowerCase(piece)) {
            case 'p' -> moves = pawnMoves(pos, pieceIsWhite, occupied, friendlyPieces, enemyPieces);
            case 'n' -> {
                moves = knightMoves(pos);
                moves &= ~friendlyPieces;
            }
            case 'b' -> {
                moves = availableMoves(idx, new int[]{9, 7, -7, -9}, occupied);
                moves &= ~friendlyPieces;
            }
            case 'r' -> {
                moves = availableMoves(idx, new int[]{8, -8, 1, -1}, occupied);
                moves &= ~friendlyPieces;
            }
            case 'q' -> {
                moves = availableMoves(idx, new int[]{8, -8, 1, -1, 9, 7, -7, -9}, occupied);
                moves &= ~friendlyPieces;
            }
            case 'k' -> {
                moves = kingMoves(pos);
                moves &= ~friendlyPieces;
            }
            default -> throw new IllegalArgumentException("Invalid piece " + piece);
        }
        return moves;
    }

    private long pawnMoves(long pos, boolean isWhite, long occupied, long friendlyPieces, long enemyPieces) {
        long moves = 0L;
        long notA = 0xfefefefefefefefeL;
        long notH = 0x7f7f7f7f7f7f7f7fL;
        long singlePush;
        if (isWhite) {
            singlePush = (pos << 8) & ~occupied;
            moves |= singlePush;
            long rank2 = 0x000000000000FF00L;
            if ((pos & rank2) != 0) {
                long doublePush = (pos << 16) & ~occupied & ~((pos << 8));
                moves |= doublePush;
            }
            long capLeft = (pos & notA) << 7;
            long capRight = (pos & notH) << 9;
            capLeft &= enemyPieces;
            capRight &= enemyPieces;
            moves |= capLeft | capRight;
        } else {
            singlePush = (pos >>> 8) & ~occupied;
            moves |= singlePush;
            long rank7 = 0x00FF000000000000L;
            if ((pos & rank7) != 0) {
                long doublePush = (pos >>> 16) & ~occupied & ~((pos >>> 8));
                moves |= doublePush;
            }
            long capLeft = (pos & notA) >>> 9;
            long capRight = (pos & notH) >>> 7;
            capLeft &= enemyPieces;
            capRight &= enemyPieces;
            moves |= capLeft | capRight;
        }
        moves &= ~friendlyPieces;
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

    private long availableMoves(int idx, int[] dirs, long occupied) {
        long result = 0L;
        int rank = idx / 8;
        int file = idx % 8;

        for (int dir: dirs) {
            int r = rank;
            int f = file;

            while (true) {
                int dr = dir / 8;
                int df = dir % 8;

                if (dir == 9) { dr = 1; df = -1; }
                else if (dir == 7) { dr = 1; df = -1; }
                else if (dir == -7) { dr = -1; df = 1; }
                else if (dir == -9) { dr = -1; df = -1; }
                else if (dir == 8) { dr = 1; df = 0; }
                else if (dir == -8) { dr = -1; df = 0; }
                else if (dir == 1) { dr = 0; df = 1; }
                else if (dir == -1) { dr = 0; df = -1; }

                r += dr;
                f += df;

                if (r < 0 || r > 7 || f < 0 || f > 7) break;

                int sq = r * 8 + f;
                long mask = 1L << sq;
                result |= mask;

                if ((occupied & (1L << sq)) != 0) break;
            }
        }
        return result;
    }

    public void pieceSees(String square) {
        char piece = getPieceAt(square);
        if (piece == '.') {
            System.out.println("No piece found");
            return;
        }

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
                else {
                    char c = getPieceChar(mask);
                    System.out.print(c + " ");
                }
            }
            System.out.println(" " + (rank + 1));
        }
        System.out.println("  a b c d e f g h");
    }

    public void loadDefault() {
        set("Ra1"); set("Nb1"); set("Bc1"); set("Qd1"); set("Ke1"); set("Bf1"); set("Ng1"); set("Rh1");
        set("Pa2"); set("Pb2"); set("Pc2"); set("Pd2"); set("Pe2"); set("Pf2"); set("Pg2"); set("Ph2");
        whiteToMove = false;

        set("Ra8"); set("Nb8"); set("Bc8"); set("Qd8"); set("Ke8"); set("Bf8"); set("Ng8"); set("Rh8");
        set("Pa7"); set("Pb7"); set("Pc7"); set("Pd7"); set("Pe7"); set("Pf7"); set("Pg7"); set("Ph7");
        whiteToMove = true;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }
}