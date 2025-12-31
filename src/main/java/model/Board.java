package model;

import io.Parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;




public class Board {
    
    private final long[] bb = new long[12];
    
    
    private static final int WP = 0;
    
    private static final int WN = 1;
    
    private static final int WB = 2;
    
    private static final int WR = 3;
    
    private static final int WQ = 4;
    
    private static final int WK = 5;
    
    private static final int BP = 6;
    
    private static final int BN = 7;
    
    private static final int BB = 8;
    
    private static final int BR = 9;
    
    private static final int BQ = 10;
    
    private static final int BK = 11;

    
    public Color sideToMove = Color.WHITE;
    
    
    private int enPassantSquare = -1;
    
    
    private int castlingRights = 0b1111;
    
    
    public int lastMoveFrom = -1;
    
    
    public int lastMoveTo = -1;

    
    private Stack<BoardState> history = new Stack<>();
    
    
    public List<Piece> capturedByWhite = new ArrayList<>();
    
    
    public List<Piece> capturedByBlack = new ArrayList<>();

    
    private static final MagicBitboards magicBitboards = new MagicBitboards();

    
    public Board() {
        setupInitialPosition();
    }

    
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

    
    private void saveState() {
        BoardState state = new BoardState();
        state.bb = bb.clone();
        state.sideToMove = sideToMove;
        state.enPassantSquare = enPassantSquare;
        state.castlingRights = castlingRights;
        state.lastMoveFrom = lastMoveFrom;
        state.lastMoveTo = lastMoveTo;
        state.capturedByWhite = new ArrayList<>(capturedByWhite);
        state.capturedByBlack = new ArrayList<>(capturedByBlack);
        history.push(state);
    }

    
    public boolean undo() {
        if (history.isEmpty()) return false;

        BoardState state = history.pop();
        System.arraycopy(state.bb, 0, bb, 0, 12);
        sideToMove = state.sideToMove;
        enPassantSquare = state.enPassantSquare;
        castlingRights = state.castlingRights;
        lastMoveFrom = state.lastMoveFrom;
        lastMoveTo = state.lastMoveTo;
        capturedByWhite = state.capturedByWhite;
        capturedByBlack = state.capturedByBlack;

        return true;
    }

    
    public boolean canUndo() { 
        return !history.isEmpty(); 
    }

    
    private int sqIdx(String sq) {
        return (sq.charAt(1) - '1') * 8 + (sq.charAt(0) - 'a');
    }

    
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
        for (int i = 0; i < 12; i++) 
            if ((bb[i] & mask) != 0)
                return i;
        return -1;
    }

    
    public Piece getPieceAt(int idx) {
        int p = pieceIndexAt(idx);
        if (p == -1) return null;
        return new Piece(Type.values()[p % 6], p < 6 ? Color.WHITE : Color.BLACK);
    }

    
    private int getPieceIndex(Type type, Color color) {
        return type.ordinal() + (color == Color.WHITE ? 0 : 6);
    }

    
    private long getAttacks(int square, Type type, long occ) {
        return switch (type) {
            case BISHOP -> magicBitboards.getBishopAttacks(square, occ);
            case ROOK -> magicBitboards.getRookAttacks(square, occ);
            case QUEEN -> magicBitboards.getBishopAttacks(square, occ) 
                        | magicBitboards.getRookAttacks(square, occ);
            case KNIGHT -> generateKnightMoves(square);
            case KING -> generateKingMoves(square);
            case PAWN -> 0L; // Pawns handled separately
        };
    }

    
    private boolean isSquareAttacked(int square, Color byColor) {
        long occ = allOcc();
        long attackers = byColor == Color.WHITE ? whiteOcc() : blackOcc();

        // Check pawns
        int pawnIdx = byColor == Color.WHITE ? WP : BP;
        long pawnAttacks = byColor == Color.WHITE 
            ? generatePawnAttacks(square, Color.BLACK)
            : generatePawnAttacks(square, Color.WHITE);
        if ((bb[pawnIdx] & pawnAttacks) != 0) return true;

        // Check knights
        int knightIdx = byColor == Color.WHITE ? WN : BN;
        if ((bb[knightIdx] & generateKnightMoves(square)) != 0) return true;

        // Check bishops/queens (diagonal)
        long bishopQueens = bb[byColor == Color.WHITE ? WB : BB] 
                          | bb[byColor == Color.WHITE ? WQ : BQ];
        if ((bishopQueens & magicBitboards.getBishopAttacks(square, occ)) != 0) return true;

        // Check rooks/queens (straight)
        long rookQueens = bb[byColor == Color.WHITE ? WR : BR] 
                        | bb[byColor == Color.WHITE ? WQ : BQ];
        if ((rookQueens & magicBitboards.getRookAttacks(square, occ)) != 0) return true;

        // Check king
        int kingIdx = byColor == Color.WHITE ? WK : BK;
        if ((bb[kingIdx] & generateKingMoves(square)) != 0) return true;

        return false;
    }

    
    private int findKing(Color color) {
        long kingBB = bb[color == Color.WHITE ? WK : BK];
        return Long.numberOfTrailingZeros(kingBB);
    }

    
    public boolean isInCheck(Color color) {
        int kingSquare = findKing(color);
        return isSquareAttacked(kingSquare, color.opposite());
    }

    
    private long generatePawnAttacks(int square, Color color) {
        long attacks = 0L;
        int[] offsets = color == Color.WHITE ? new int[]{7, 9} : new int[]{-7, -9};
        for (int off : offsets) {
            int to = square + off;
            if (to >= 0 && to < 64 && Math.abs((to % 8) - (square % 8)) == 1) {
                attacks |= 1L << to;
            }
        }
        return attacks;
    }

    
    private long generatePawnMoves(int from, Color color) {
        long moves = 0;
        int dir = color == Color.WHITE ? 8 : -8;
        int rank = from / 8;
        long occ = allOcc();
        long enemy = color == Color.WHITE ? blackOcc() : whiteOcc();

        // Forward move
        int fwd = from + dir;
        if (fwd >= 0 && fwd < 64 && (occ & (1L << fwd)) == 0) {
            moves |= 1L << fwd;
            // Double push
            if ((color == Color.WHITE && rank == 1) || (color == Color.BLACK && rank == 6)) {
                int dbl = from + 2 * dir;
                if ((occ & (1L << dbl)) == 0)
                    moves |= 1L << dbl;
            }
        }

        // Captures
        for (int off : color == Color.WHITE ? new int[]{7, 9} : new int[]{-7, -9}) {
            int to = from + off;
            if (to >= 0 && to < 64 && Math.abs((to % 8) - (from % 8)) == 1) {
                if (((enemy & (1L << to)) != 0) || to == enPassantSquare) {
                    moves |= 1L << to;
                }
            }
        }

        return moves;
    }

    
    private long generateKnightMoves(int from) {
        long m = 0;
        int[] offsets = {-17, -15, -10, -6, 6, 10, 15, 17};
        for (int d : offsets) {
            int t = from + d;
            if (t >= 0 && t < 64 && Math.abs((from % 8) - (t % 8)) <= 2)
                m |= 1L << t;
        }
        return m;
    }

    
    private long generateKingMoves(int from) {
        long m = 0;
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        for (int d : offsets) {
            int t = from + d;
            if (t >= 0 && t < 64 && Math.abs((from % 8) - (t % 8)) <= 1)
                m |= 1L << t;
        }
        return m;
    }

    
    private long generateMoves(int from, Type type) {
        long occ = allOcc();
        long friendly = sideToMove == Color.WHITE ? whiteOcc() : blackOcc();

        return switch (type) {
            case PAWN -> generatePawnMoves(from, sideToMove);
            case KNIGHT -> generateKnightMoves(from) & ~friendly;
            case BISHOP -> magicBitboards.getBishopAttacks(from, occ) & ~friendly;
            case ROOK -> magicBitboards.getRookAttacks(from, occ) & ~friendly;
            case QUEEN -> (magicBitboards.getBishopAttacks(from, occ)
                         | magicBitboards.getRookAttacks(from, occ)) & ~friendly;
            case KING -> generateKingMoves(from) & ~friendly;
        };
    }

    
    private boolean isLegalMove(int from, int to, Type pieceType) {
        long[] bbCopy = bb.clone();
        int epCopy = enPassantSquare;
        
        int pieceIdx = getPieceIndex(pieceType, sideToMove);
        bb[pieceIdx] &= ~(1L << from);
        bb[pieceIdx] |= 1L << to;
        
        // Handle capture
        int captureIdx = pieceIndexAt(to);
        if (captureIdx != -1 && captureIdx != pieceIdx) {
            bb[captureIdx] &= ~(1L << to);
        }
        
        // Handle en passant capture
        if (pieceType == Type.PAWN && to == enPassantSquare) {
            int epCaptureSquare = sideToMove == Color.WHITE ? to - 8 : to + 8;
            int epCaptureIdx = sideToMove == Color.WHITE ? BP : WP;
            bb[epCaptureIdx] &= ~(1L << epCaptureSquare);
        }
        
        // Check if king is in check
        boolean legal = !isInCheck(sideToMove);
        
        // Restore state
        System.arraycopy(bbCopy, 0, bb, 0, 12);
        enPassantSquare = epCopy;
        
        return legal;
    }

    
    private boolean canMoveTo(int from, int target, Type type) {
        long moves = generateMoves(from, type);
        if ((moves & (1L << target)) == 0) return false;
        return isLegalMove(from, target, type);
    }

    
    public List<Move> generateAllLegalMoves() {
        List<Move> moves = new ArrayList<>();
        int startIdx = sideToMove == Color.WHITE ? 0 : 6;
        
        for (int pieceIdx = startIdx; pieceIdx < startIdx + 6; pieceIdx++) {
            long pieces = bb[pieceIdx];
            Type type = Type.values()[pieceIdx % 6];
            
            while (pieces != 0) {
                int from = Long.numberOfTrailingZeros(pieces);
                pieces &= pieces - 1; // Clear lowest bit
                
                long possibleMoves = generateMoves(from, type);
                while (possibleMoves != 0) {
                    int to = Long.numberOfTrailingZeros(possibleMoves);
                    possibleMoves &= possibleMoves - 1;
                    
                    if (isLegalMove(from, to, type)) {
                        Move move = new Move();
                        move.piece = new Piece(type, sideToMove);
                        move.target = to;
                        move.disambiguation = from;
                        
                        // Set flags
                        if (pieceIndexAt(to) != -1) {
                            move.flags |= Move.FLAG_CAPTURE;
                        }
                        if (type == Type.PAWN && (to / 8 == 0 || to / 8 == 7)) {
                            move.flags |= Move.FLAG_PROMOTION;
                        }
                        if (type == Type.PAWN && to == enPassantSquare) {
                            move.flags |= Move.FLAG_EN_PASSANT;
                        }
                        
                        moves.add(move);
                    }
                }
            }
        }
        
        // Add castling moves
        moves.addAll(generateCastlingMoves());
        
        return moves;
    }

    
    private List<Move> generateCastlingMoves() {
        List<Move> castles = new ArrayList<>();
        
        if (sideToMove == Color.WHITE) {
            // Kingside castling
            if ((castlingRights & 0b1000) != 0 && 
                (allOcc() & 0x0000000000000060L) == 0 &&
                !isSquareAttacked(4, Color.BLACK) &&
                !isSquareAttacked(5, Color.BLACK) &&
                !isSquareAttacked(6, Color.BLACK)) {
                
                Move move = new Move();
                move.piece = new Piece(Type.KING, Color.WHITE);
                move.flags = Move.FLAG_SHORT_CASTLE;
                castles.add(move);
            }
            
            // Queenside castling
            if ((castlingRights & 0b0100) != 0 && 
                (allOcc() & 0x000000000000000EL) == 0 &&
                !isSquareAttacked(4, Color.BLACK) &&
                !isSquareAttacked(3, Color.BLACK) &&
                !isSquareAttacked(2, Color.BLACK)) {
                
                Move move = new Move();
                move.piece = new Piece(Type.KING, Color.WHITE);
                move.flags = Move.FLAG_LONG_CASTLE;
                castles.add(move);
            }
        } else {
            // Black castling
            if ((castlingRights & 0b0010) != 0 && 
                (allOcc() & 0x6000000000000000L) == 0 &&
                !isSquareAttacked(60, Color.WHITE) &&
                !isSquareAttacked(61, Color.WHITE) &&
                !isSquareAttacked(62, Color.WHITE)) {
                
                Move move = new Move();
                move.piece = new Piece(Type.KING, Color.BLACK);
                move.flags = Move.FLAG_SHORT_CASTLE;
                castles.add(move);
            }
            
            if ((castlingRights & 0b0001) != 0 && 
                (allOcc() & 0x0E00000000000000L) == 0 &&
                !isSquareAttacked(60, Color.WHITE) &&
                !isSquareAttacked(59, Color.WHITE) &&
                !isSquareAttacked(58, Color.WHITE)) {
                
                Move move = new Move();
                move.piece = new Piece(Type.KING, Color.BLACK);
                move.flags = Move.FLAG_LONG_CASTLE;
                castles.add(move);
            }
        }
        
        return castles;
    }

    
    public boolean isCheckmate() {
        return isInCheck(sideToMove) && generateAllLegalMoves().isEmpty();
    }

    
    public boolean isStalemate() {
        return !isInCheck(sideToMove) && generateAllLegalMoves().isEmpty();
    }

    
    public void move(String input) throws IllegalMoveException {
        input = input.trim();

        if (input.matches("[a-h][1-8]\\s+[a-h][1-8]")) {
            String[] parts = input.split("\\s+");
            move(parts[0], parts[1]);
            return;
        }

        Parser parser = new Parser();
        Move move = parser.parseSingleMove(input, sideToMove);
        resolveAndMakeMove(move);
    }

    
    public void move(String from, String to) throws IllegalMoveException {
        int fromIdx = sqIdx(from);
        int toIdx = sqIdx(to);

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
        move.target = toIdx;
        move.disambiguation = fromIdx;

        if (piece.type() == Type.PAWN) {
            int targetRank = toIdx / 8;
            if (targetRank == 7 || targetRank == 0) {
                move.flags |= Move.FLAG_PROMOTION;
            }
            if (Math.abs(fromIdx % 8 - toIdx % 8) == 1 && getPieceAt(toIdx) == null && toIdx == enPassantSquare) {
                move.flags |= Move.FLAG_EN_PASSANT;
            }
        }

        if (pieceIndexAt(toIdx) != -1) {
            move.flags |= Move.FLAG_CAPTURE;
        }

        saveState();
        lastMoveFrom = fromIdx;
        lastMoveTo = toIdx;
        makeMove(move);
        
        // Check for check/mate
        if (isInCheck(sideToMove)) {
            move.flags |= Move.FLAG_CHECK;
            if (isCheckmate()) {
                move.flags |= Move.FLAG_MATE;
            }
        }
    }

    
    public void resolveAndMakeMove(Move move) throws IllegalMoveException {
        if ((move.flags & Move.FLAG_SHORT_CASTLE) != 0 ||
            (move.flags & Move.FLAG_LONG_CASTLE) != 0) {
            
            if (sideToMove == Color.WHITE) {
                lastMoveFrom = 4;
                lastMoveTo = (move.flags & Move.FLAG_SHORT_CASTLE) != 0 ? 6 : 2;
            } else {
                lastMoveFrom = 60;
                lastMoveTo = (move.flags & Move.FLAG_SHORT_CASTLE) != 0 ? 62 : 58;
            }
            
            saveState();
            makeMove(move);
            return;
        }

        int target = move.target & 0xFF;
        Type pieceType = move.piece.type();
        int pieceIdx = getPieceIndex(pieceType, sideToMove);

        List<Integer> candidates = new ArrayList<>();
        long pieceBitboard = bb[pieceIdx];

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

        if (move.disambiguation >= 0 && move.disambiguation < 8) {
            int file = move.disambiguation;
            candidates.removeIf(sq -> sq % 8 != file);
        } else if (move.disambiguation >= 8 && move.disambiguation < 16) {
            int rank = move.disambiguation - 8;
            candidates.removeIf(sq -> sq / 8 != rank);
        }

        if (candidates.size() != 1) {
            throw new IllegalMoveException("Ambiguous move");
        }

        int fromSquare = candidates.get(0);
        move.disambiguation = fromSquare;

        // Check if this is an en passant move
        if (pieceType == Type.PAWN && Math.abs(fromSquare % 8 - target % 8) == 1 && 
            getPieceAt(target) == null && target == enPassantSquare) {
            move.flags |= Move.FLAG_EN_PASSANT;
        }

        saveState();
        lastMoveFrom = fromSquare;
        lastMoveTo = target;
        makeMove(move);
        
        // Check for check/mate
        if (isInCheck(sideToMove)) {
            move.flags |= Move.FLAG_CHECK;
            if (isCheckmate()) {
                move.flags |= Move.FLAG_MATE;
            }
        }
    }

    
    private String squareToString(int sq) {
        return "" + (char)('a' + sq % 8) + (char)('1' + sq / 8);
    }

    
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

        // Update castling rights when king or rook moves
        if (move.piece.type() == Type.KING) {
            if (sideToMove == Color.WHITE) {
                castlingRights &= 0b0011;
            } else {
                castlingRights &= 0b1100;
            }
        }
        if (move.piece.type() == Type.ROOK) {
            if (sideToMove == Color.WHITE) {
                if (from == 0) castlingRights &= ~0b0100;
                if (from == 7) castlingRights &= ~0b1000;
            } else {
                if (from == 56) castlingRights &= ~0b0001;
                if (from == 63) castlingRights &= ~0b0010;
            }
        }

        bb[pieceIdx] &= ~(1L << from);

        if ((move.flags & Move.FLAG_EN_PASSANT) != 0) {
            int cap = sideToMove == Color.WHITE ? to - 8 : to + 8;
            Piece capturedPiece = new Piece(Type.PAWN, sideToMove.opposite());
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
                
                // Update castling rights when rook is captured
                if (capturedPiece.type() == Type.ROOK) {
                    if (capturedPiece.color() == Color.WHITE) {
                        if (to == 0) castlingRights &= ~0b0100;
                        if (to == 7) castlingRights &= ~0b1000;
                    } else {
                        if (to == 56) castlingRights &= ~0b0001;
                        if (to == 63) castlingRights &= ~0b0010;
                    }
                }
            }
        }

        if ((move.flags & Move.FLAG_PROMOTION) != 0) {
            bb[sideToMove == Color.WHITE ? WQ : BQ] |= 1L << to;
        } else {
            bb[pieceIdx] |= 1L << to;
        }

        // Set en passant square if pawn double push
        if (move.piece.type() == Type.PAWN && Math.abs(to - from) == 16) {
            enPassantSquare = (from + to) / 2;
        } else {
            enPassantSquare = -1;
        }

        sideToMove = sideToMove.opposite();
    }

    
    private void executeCastling(boolean kingside) {
        if (sideToMove == Color.WHITE) {
            if (kingside) {
                bb[WK] = 0x0000000000000040L;
                bb[WR] &= ~0x0000000000000080L;
                bb[WR] |= 0x0000000000000020L;
                castlingRights &= 0b0011;
            } else {
                bb[WK] = 0x0000000000000004L;
                bb[WR] &= ~0x0000000000000001L;
                bb[WR] |= 0x0000000000000008L;
                castlingRights &= 0b0011;
            }
        } else {
            if (kingside) {
                bb[BK] = 0x4000000000000000L;
                bb[BR] &= ~0x8000000000000000L;
                bb[BR] |= 0x2000000000000000L;
                castlingRights &= 0b1100;
            } else {
                bb[BK] = 0x0400000000000000L;
                bb[BR] &= ~0x0100000000000000L;
                bb[BR] |= 0x0800000000000000L;
                castlingRights &= 0b1100;
            }
        }
    }

    
    public void display() {
        for (int r = 7; r >= 0; r--) {
            for (int f = 0; f < 8; f++) {
                Piece p = getPieceAt(r * 8 + f);
                System.out.print(p == null ? ". " : p + " ");
            }
            System.out.println();
        }
        
        if (isInCheck(sideToMove)) {
            System.out.println(sideToMove + " is in check!");
        }
        if (isCheckmate()) {
            System.out.println("Checkmate! " + sideToMove.opposite() + " wins!");
        }
        if (isStalemate()) {
            System.out.println("Stalemate!");
        }
    }
    
    
    public GameStatus getGameStatus() {
        if (isCheckmate()) {
            return GameStatus.CHECKMATE;
        }
        if (isStalemate()) {
            return GameStatus.STALEMATE;
        }
        if (isInCheck(sideToMove)) {
            return GameStatus.CHECK;
        }
        return GameStatus.ONGOING;
    }
    
    
    public enum GameStatus {
        ONGOING,
        CHECK,
        CHECKMATE, 
        STALEMATE
    }

    
    public class IllegalMoveException extends Exception {
        public IllegalMoveException(String message) { super(message); }
    }

    
    private static class BoardState {
        
        long[] bb;
        
        Color sideToMove;
        
        int enPassantSquare;
        
        int castlingRights;
        
        int lastMoveFrom;
        
        int lastMoveTo;
        
        List<Piece> capturedByWhite;
        
        List<Piece> capturedByBlack;
    }
}
