package model;

import io.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Board {

    private final long[] bb = new long[12];
    private static final int WP = 0, WN = 1, WB = 2, WR = 3, WQ = 4, WK = 5;
    private static final int BP = 6, BN = 7, BB = 8, BR = 9, BQ = 10, BK = 11;

    public Color sideToMove = Color.WHITE;

    private int enPassantSquare = -1;
    private boolean wkCastle = true, wqCastle = true, bkCastle = true, bqCastle = true;
    
    public int lastMoveFrom = -1;
    public int lastMoveTo = -1;
    
    // История на позициите за undo
    private Stack<BoardState> history = new Stack<>();
    
    // Взети фигури
    public List<Piece> capturedByWhite = new ArrayList<>();
    public List<Piece> capturedByBlack = new ArrayList<>();

    public Board() {
        setupInitialPosition();
    }

    /* =========================
       Setup
       ========================= */

    private void setupInitialPosition() {
        bb[WP] = 0x000000000000FF00L;
        bb[BP] = 0x00FF000000000000L;
        bb[WR] = 0x0000000000000081L;
        bb[BR] = 0x8100000000000000L;
        bb[WN] = 0x0000000000000042L;
        bb[BN] = 0x4200000000000000L;
        bb[WB] = 0x0000000000000024L;
        bb[BB] = 0x2400000000000000L;
        bb[WQ] = 0x0000000000000008L;
        bb[BQ] = 0x0800000000000000L;
        bb[WK] = 0x0000000000000010L;
        bb[BK] = 0x1000000000000000L;
    }

    /* =========================
       UNDO
       ========================= */

    /**
     * Запазва текущата позиция в историята
     */
    private void saveState() {
        BoardState state = new BoardState();
        state.bb = bb.clone();
        state.sideToMove = sideToMove;
        state.enPassantSquare = enPassantSquare;
        state.wkCastle = wkCastle;
        state.wqCastle = wqCastle;
        state.bkCastle = bkCastle;
        state.bqCastle = bqCastle;
        state.lastMoveFrom = lastMoveFrom;
        state.lastMoveTo = lastMoveTo;
        state.capturedByWhite = new ArrayList<>(capturedByWhite);
        state.capturedByBlack = new ArrayList<>(capturedByBlack);
        history.push(state);
    }

    /**
     * Връща позицията на предишния ход
     */
    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        
        BoardState state = history.pop();
        System.arraycopy(state.bb, 0, bb, 0, 12);
        sideToMove = state.sideToMove;
        enPassantSquare = state.enPassantSquare;
        wkCastle = state.wkCastle;
        wqCastle = state.wqCastle;
        bkCastle = state.bkCastle;
        bqCastle = state.bqCastle;
        lastMoveFrom = state.lastMoveFrom;
        lastMoveTo = state.lastMoveTo;
        capturedByWhite = state.capturedByWhite;
        capturedByBlack = state.capturedByBlack;
        
        return true;
    }

    /**
     * Проверява дали има ходове за undo
     */
    public boolean canUndo() {
        return !history.isEmpty();
    }

    /* =========================
       Square helpers
       ========================= */

    public int sqIdx(String sq) {
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        return (rank - '1') * 8 + (file - 'a');
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

    private int pieceIndexAt(int square) {
        long mask = 1L << square;
        for (int i = 0; i < 12; i++) {
            if ((bb[i] & mask) != 0) return i;
        }
        return -1;
    }

    public Piece getPieceAt(int idx) {
        int p = pieceIndexAt(idx);
        if (p == -1) return null;
        return new Piece(Type.values()[p % 6], p < 6 ? Color.WHITE : Color.BLACK);
    }

    /* =========================
       Move generation helpers
       ========================= */

    private int getPieceIndex(Type type, Color color) {
        return type.ordinal() + (color == Color.WHITE ? 0 : 6);
    }

    private boolean canMoveTo(int from, int target, Type type) {
        return (generateMoves(from, type) & (1L << target)) != 0;
    }

    private long generateMoves(int from, Type type) {
        long occ = allOcc();
        long friendly = sideToMove == Color.WHITE ? whiteOcc() : blackOcc();

        return switch (type) {
            case PAWN -> generatePawnMoves(from, sideToMove);
            case KNIGHT -> generateKnightMoves(from) & ~friendly;
            case BISHOP -> generateBishopMoves(from, occ) & ~friendly;
            case ROOK -> generateRookMoves(from, occ) & ~friendly;
            case QUEEN -> (generateBishopMoves(from, occ)
                         | generateRookMoves(from, occ)) & ~friendly;
            case KING -> generateKingMoves(from) & ~friendly;
        };
    }

    /* =========================
       Piece move generators
       ========================= */

    private long generatePawnMoves(int from, Color color) {
        long moves = 0;
        int dir = color == Color.WHITE ? 8 : -8;
        int rank = from / 8;
        long occ = allOcc();
        long enemy = color == Color.WHITE ? blackOcc() : whiteOcc();

        int fwd = from + dir;
        if ((occ & (1L << fwd)) == 0) {
            moves |= 1L << fwd;
            if ((color == Color.WHITE && rank == 1) || (color == Color.BLACK && rank == 6)) {
                int dbl = from + 2 * dir;
                if ((occ & (1L << dbl)) == 0) moves |= 1L << dbl;
            }
        }

        for (int off : color == Color.WHITE ? new int[]{7,9} : new int[]{-7,-9}) {
            int to = from + off;
            if (to >= 0 && to < 64 &&
                Math.abs((to % 8) - (from % 8)) == 1 &&
                (((enemy | (1L << enPassantSquare)) & (1L << to)) != 0)) {
                moves |= 1L << to;
            }
        }

        return moves;
    }

    private long generateKnightMoves(int from) {
        long m = 0;
        int[] o = {-17,-15,-10,-6,6,10,15,17};
        for (int d : o) {
            int t = from + d;
            if (t >= 0 && t < 64 && Math.abs((from % 8) - (t % 8)) <= 2)
                m |= 1L << t;
        }
        return m;
    }

    private long generateBishopMoves(int from, long occ) {
        return slidingMoves(from, occ, new int[]{-9,-7,7,9});
    }

    private long generateRookMoves(int from, long occ) {
        return slidingMoves(from, occ, new int[]{-8,-1,1,8});
    }

    private long generateKingMoves(int from) {
        long m = 0;
        int[] o = {-9,-8,-7,-1,1,7,8,9};
        for (int d : o) {
            int t = from + d;
            if (t >= 0 && t < 64 && Math.abs((from % 8) - (t % 8)) <= 1)
                m |= 1L << t;
        }
        return m;
    }

    private long slidingMoves(int from, long occ, int[] dirs) {
        long m = 0;
        for (int d : dirs) {
            int t = from + d;
            while (t >= 0 && t < 64 &&
                   Math.abs((t % 8) - (from % 8)) <= 2) {
                m |= 1L << t;
                if ((occ & (1L << t)) != 0) break;
                t += d;
            }
        }
        return m;
    }

    /* =========================
       SAN MOVE HANDLING (NEW)
       ========================= */

    /**
     * Приема SAN нотация (e4, Nf3, O-O) или координатна (e2 e4)
     */
    public void move(String input) throws IllegalMoveException {
        input = input.trim();
        
        // Проверка за координатна нотация (e2 e4)
        if (input.matches("[a-h][1-8]\\s+[a-h][1-8]")) {
            String[] parts = input.split("\\s+");
            move(parts[0], parts[1]);
            return;
        }
        
        // SAN нотация
        Parser parser = new Parser();
        Move move = parser.parseSingleMove(input, sideToMove);
        resolveAndMakeMove(move);
    }

    /**
     * Координатно местене
     */
    public void move(String from, String to) throws IllegalMoveException {
        saveState(); // Запазваме състоянието преди хода
        
        int fromIdx = sqIdx(from);
        int toIdx = sqIdx(to);
        
        lastMoveFrom = fromIdx;
        lastMoveTo = toIdx;
        
        Piece piece = getPieceAt(fromIdx);
        if (piece == null) {
            throw new IllegalMoveException("No piece at " + from);
        }
        if (piece.color() != sideToMove) {
            throw new IllegalMoveException("Not your piece");
        }
        
        if (!canMoveTo(fromIdx, toIdx, piece.type())) {
            throw new IllegalMoveException("Illegal move");
        }
        
        Move move = new Move();
        move.piece = piece;
        move.target = (byte) toIdx;
        move.disambiguation = fromIdx;
        
        // Проверка за специални ходове
        if (piece.type() == Type.PAWN) {
            int targetRank = toIdx / 8;
            if (targetRank == 7 || targetRank == 0) {
                move.flags |= Move.FLAG_PROMOTION;
            }
            if (Math.abs(fromIdx % 8 - toIdx % 8) == 1 && getPieceAt(toIdx) == null) {
                move.flags |= Move.FLAG_EN_PASSANT;
            }
        }
        
        makeMove(move);
    }

    /**
     * Разрешава SAN хода до конкретна позиция
     */
    private void resolveAndMakeMove(Move move) throws IllegalMoveException {
        saveState(); // Запазваме състоянието преди хода
        
        // Рокада
        if ((move.flags & Move.FLAG_SHORT_CASTLE) != 0 || 
            (move.flags & Move.FLAG_LONG_CASTLE) != 0) {
            // За рокада, запомняме позициите
            if (sideToMove == Color.WHITE) {
                lastMoveFrom = 4; // e1
                lastMoveTo = (move.flags & Move.FLAG_SHORT_CASTLE) != 0 ? 6 : 2; // g1 или c1
            } else {
                lastMoveFrom = 60; // e8
                lastMoveTo = (move.flags & Move.FLAG_SHORT_CASTLE) != 0 ? 62 : 58; // g8 или c8
            }
            makeMove(move);
            return;
        }

        int target = move.target & 0xFF;
        Type pieceType = move.piece.type();
        int pieceIdx = getPieceIndex(pieceType, sideToMove);
        
        List<Integer> candidates = new ArrayList<>();
        long pieceBitboard = bb[pieceIdx];
        
        // Намери всички фигури от този тип
        for (int sq = 0; sq < 64; sq++) {
            if ((pieceBitboard & (1L << sq)) != 0) {
                if (canMoveTo(sq, target, pieceType)) {
                    candidates.add(sq);
                }
            }
        }
        
        if (candidates.isEmpty()) {
            throw new IllegalMoveException("No piece can reach " + squareToString(target));
        }
        
        // Филтрирай по disambiguation
        if (move.disambiguation >= 0 && move.disambiguation < 8) {
            // File disambiguation (a-h)
            int file = move.disambiguation;
            candidates.removeIf(sq -> sq % 8 != file);
        } else if (move.disambiguation >= 8 && move.disambiguation < 16) {
            // Rank disambiguation (1-8)
            int rank = move.disambiguation - 8;
            candidates.removeIf(sq -> sq / 8 != rank);
        }
        
        if (candidates.size() != 1) {
            throw new IllegalMoveException("Ambiguous move");
        }
        
        int fromSquare = candidates.get(0);
        move.disambiguation = fromSquare;
        
        // Запомняме позициите
        lastMoveFrom = fromSquare;
        lastMoveTo = target;
        
        makeMove(move);
    }

    private String squareToString(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }

    /* =========================
       APPLY MOVE (CORE)
       ========================= */

    public void makeMove(Move move) {
        int from = move.disambiguation;
        int to = move.target & 0xFF;
        int pieceIdx = getPieceIndex(move.piece.type(), move.piece.color());

        if ((move.flags & Move.FLAG_SHORT_CASTLE) != 0) {
            executeCastling(true);
            sideToMove = sideToMove.opposite();
            return;
        }
        if ((move.flags & Move.FLAG_LONG_CASTLE) != 0) {
            executeCastling(false);
            sideToMove = sideToMove.opposite();
            return;
        }

        bb[pieceIdx] &= ~(1L << from);

        if ((move.flags & Move.FLAG_EN_PASSANT) != 0) {
            int cap = sideToMove == Color.WHITE ? to - 8 : to + 8;
            Piece capturedPiece = new Piece(Type.PAWN, sideToMove == Color.WHITE ? Color.BLACK : Color.WHITE);
            if (sideToMove == Color.WHITE) {
                capturedByWhite.add(capturedPiece);
            } else {
                capturedByBlack.add(capturedPiece);
            }
            bb[sideToMove == Color.WHITE ? BP : WP] &= ~(1L << cap);
        } else {
            int capIdx = pieceIndexAt(to);
            if (capIdx != -1) {
                Piece capturedPiece = getPieceAt(to);
                if (sideToMove == Color.WHITE) {
                    capturedByWhite.add(capturedPiece);
                } else {
                    capturedByBlack.add(capturedPiece);
                }
                bb[capIdx] &= ~(1L << to);
            }
        }

        if ((move.flags & Move.FLAG_PROMOTION) != 0) {
            bb[sideToMove == Color.WHITE ? WQ : BQ] |= 1L << to;
        } else {
            bb[pieceIdx] |= 1L << to;
        }

        enPassantSquare = (move.piece.type() == Type.PAWN && Math.abs(to - from) == 16)
                ? (sideToMove == Color.WHITE ? from + 8 : from - 8)
                : -1;

        sideToMove = sideToMove.opposite();
    }

    private void executeCastling(boolean kingside) {
        if (sideToMove == Color.WHITE) {
            if (kingside) {
                bb[WK] = 0x0000000000000040L; // g1
                bb[WR] &= ~0x0000000000000080L; // clear h1
                bb[WR] |= 0x0000000000000020L; // f1
            } else {
                bb[WK] = 0x0000000000000004L; // c1
                bb[WR] &= ~0x0000000000000001L; // clear a1
                bb[WR] |= 0x0000000000000008L; // d1
            }
        } else {
            if (kingside) {
                bb[BK] = 0x4000000000000000L; // g8
                bb[BR] &= ~0x8000000000000000L; // clear h8
                bb[BR] |= 0x2000000000000000L; // f8
            } else {
                bb[BK] = 0x0400000000000000L; // c8
                bb[BR] &= ~0x0100000000000000L; // clear a8
                bb[BR] |= 0x0800000000000000L; // d8
            }
        }
    }

    /* =========================
       Debug
       ========================= */

    public void display() {
        for (int r = 7; r >= 0; r--) {
            for (int f = 0; f < 8; f++) {
                Piece p = getPieceAt(r * 8 + f);
                System.out.print(p == null ? ". " : p + " ");
            }
            System.out.println();
        }
    }
    
    public class IllegalMoveException extends Exception {
        public IllegalMoveException(String message) {
            super(message);
        }
    }
    
    /**
     * Клас за запазване на състоянието на борда
     */
    private static class BoardState {
        long[] bb;
        Color sideToMove;
        int enPassantSquare;
        boolean wkCastle, wqCastle, bkCastle, bqCastle;
        int lastMoveFrom;
        int lastMoveTo;
        List<Piece> capturedByWhite;
        List<Piece> capturedByBlack;
    }
}