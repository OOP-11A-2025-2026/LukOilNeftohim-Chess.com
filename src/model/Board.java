package model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board {
    private final long[] bb = new long[12];
    private static final int WP = 0, WN = 1, WB = 2, WR = 3, WQ = 4, WK = 5;
    private static final int BP = 6, BN = 7, BB = 8, BR = 9, BQ = 10, BK = 11;
    
    public Color sideToMove = Color.WHITE;
    private int enPassantSquare = -1;
    private boolean wkCastle = true, wqCastle = true, bkCastle = true, bqCastle = true;
    
    // SAN parsing pattern
    private static final Pattern SAN_PATTERN = Pattern.compile(
        "^([NBRQK])?([a-h])?([1-8])?(x)?([a-h][1-8])(=[NBRQ])?(\\+|#)?$|^O-O(-O)?$"
    );
    
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
    
    public int sqIdx(String sq) {
        if (sq == null || sq.length() != 2)
            throw new IllegalArgumentException("Invalid square: " + sq);
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8')
            throw new IllegalArgumentException("Square out of bounds: " + sq);
        return (rank - '1') * 8 + (file - 'a');
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
        for (int i = 0; i < 12; i++) {
            if ((bb[i] & mask) != 0) return i;
        }
        return -1;
    }
    
    public Piece getPieceAt(String sq) {
        return getPieceAt(sqIdx(sq));
    }
    
    public Piece getPieceAt(int idx) {
        if (idx < 0 || idx > 63) return null;
        int pieceIdx = pieceIndexAt(idx);
        if (pieceIdx == -1) return null;
        Type type = Type.values()[pieceIdx % 6];
        Color color = pieceIdx < 6 ? Color.WHITE : Color.BLACK;
        return new Piece(type, color);
    }
    
    /**
     *Parse and validate a SAN move string.
     * Examples: "e4", "Nf3", "Bxe5", "O-O", "e8=Q+", "Raxd1"
     */
    public Move parseSAN(String san) throws IllegalMoveException {
        san = san.trim();
        
        // Handle castling
        if (san.equals("O-O") || san.equals("0-0")) {
            return createCastlingMove(true);
        }
        if (san.equals("O-O-O") || san.equals("0-0-0")) {
            return createCastlingMove(false);
        }
        
        Matcher m = SAN_PATTERN.matcher(san);
        if (!m.matches()) {
            throw new IllegalMoveException("Invalid SAN format: " + san);
        }
        
        String pieceStr = m.group(1);
        String fileHint = m.group(2);
        String rankHint = m.group(3);
        String targetSq = m.group(5);
        String promotion = m.group(6);
        String checkMate = m.group(7);
        
        Type pieceType = pieceStr == null ? Type.PAWN : parseType(pieceStr);
        int target = sqIdx(targetSq);
        
        Move move = new Move();
        move.target = (byte) target;
        move.flags = 0;
        
        if (checkMate != null) {
            if (checkMate.equals("#")) move.flags |= Move.FLAG_MATE;
            else if (checkMate.equals("+")) move.flags |= Move.FLAG_CHECK;
        }
        
        if (promotion != null) {
            move.flags |= Move.FLAG_PROMOTION;
        }
        
        // Find which piece can legally move to target
        List<Integer> candidates = findCandidates(pieceType, target, fileHint, rankHint);
        
        if (candidates.isEmpty()) {
            throw new IllegalMoveException("No legal move found for: " + san);
        }
        
        if (candidates.size() > 1) {
            throw new IllegalMoveException("Ambiguous move: " + san + " (found " + candidates.size() + " candidates)");
        }
        
        int from = candidates.get(0);
        move.piece = getPieceAt(from);
        move.disambiguation = (byte) from;
        
        // Check for en passant
        if (pieceType == Type.PAWN && target == enPassantSquare) {
            move.flags |= Move.FLAG_EN_PASSANT;
        }
        
        return move;
    }
    
    /**
     * Find all pieces of given type that can legally move to target square.
     */
    private List<Integer> findCandidates(Type type, int target, String fileHint, String rankHint) {
        List<Integer> candidates = new ArrayList<>();
        int pieceIdx = getPieceIndex(type, sideToMove);
        long pieces = bb[pieceIdx];
        
        while (pieces != 0) {
            int from = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;
            
            // Apply disambiguation hints
            if (fileHint != null) {
                int file = from % 8;
                if (file != (fileHint.charAt(0) - 'a')) continue;
            }
            if (rankHint != null) {
                int rank = from / 8;
                if (rank != (rankHint.charAt(0) - '1')) continue;
            }
            
            if (canMoveTo(from, target, type)) {
                candidates.add(from);
            }
        }
        
        return candidates;
    }
    
    /**
     * Check if piece at 'from' can legally move to 'target'.
     */
    private boolean canMoveTo(int from, int target, Type type) {
        long moves = generateMoves(from, type);
        return (moves & (1L << target)) != 0;
    }
    
    /**
     * Generate pseudo-legal moves for a piece at given square.
     */
    private long generateMoves(int from, Type type) {
        long occ = allOcc();
        long friendly = sideToMove == Color.WHITE ? whiteOcc() : blackOcc();
        
        switch (type) {
            case PAWN: return generatePawnMoves(from, sideToMove);
            case KNIGHT: return generateKnightMoves(from) & ~friendly;
            case BISHOP: return generateBishopMoves(from, occ) & ~friendly;
            case ROOK: return generateRookMoves(from, occ) & ~friendly;
            case QUEEN: return (generateBishopMoves(from, occ) | generateRookMoves(from, occ)) & ~friendly;
            case KING: return generateKingMoves(from) & ~friendly;
            default: return 0L;
        }
    }
    
    private long generatePawnMoves(int from, Color color) {
        long moves = 0L;
        int dir = color == Color.WHITE ? 8 : -8;
        int rank = from / 8;
        long occ = allOcc();
        long enemy = color == Color.WHITE ? blackOcc() : whiteOcc();
        
        // Forward move
        int to = from + dir;
        if (to >= 0 && to < 64 && (occ & (1L << to)) == 0) {
            moves |= 1L << to;
            
            // Double push
            if ((color == Color.WHITE && rank == 1) || (color == Color.BLACK && rank == 6)) {
                int to2 = from + 2 * dir;
                if ((occ & (1L << to2)) == 0) {
                    moves |= 1L << to2;
                }
            }
        }
        
        // Captures
        int[] captureOffsets = color == Color.WHITE ? new int[]{7, 9} : new int[]{-7, -9};
        for (int offset : captureOffsets) {
            to = from + offset;
            if (to >= 0 && to < 64 && Math.abs((to % 8) - (from % 8)) == 1) {
                if ((enemy & (1L << to)) != 0 || to == enPassantSquare) {
                    moves |= 1L << to;
                }
            }
        }
        
        return moves;
    }
    
    private long generateKnightMoves(int from) {
        long moves = 0L;
        int[] offsets = {-17, -15, -10, -6, 6, 10, 15, 17};
        int fromFile = from % 8;
        
        for (int offset : offsets) {
            int to = from + offset;
            if (to >= 0 && to < 64) {
                int toFile = to % 8;
                if (Math.abs(fromFile - toFile) <= 2) {
                    moves |= 1L << to;
                }
            }
        }
        return moves;
    }
    
    private long generateBishopMoves(int from, long occ) {
        return slidingMoves(from, occ, new int[]{-9, -7, 7, 9});
    }
    
    private long generateRookMoves(int from, long occ) {
        return slidingMoves(from, occ, new int[]{-8, -1, 1, 8});
    }
    
    private long generateKingMoves(int from) {
        long moves = 0L;
        int[] offsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        int fromFile = from % 8;
        
        for (int offset : offsets) {
            int to = from + offset;
            if (to >= 0 && to < 64) {
                int toFile = to % 8;
                if (Math.abs(fromFile - toFile) <= 1) {
                    moves |= 1L << to;
                }
            }
        }
        return moves;
    }
    
    private long slidingMoves(int from, long occ, int[] directions) {
        long moves = 0L;
        int fromFile = from % 8;
        
        for (int dir : directions) {
            int to = from + dir;
            while (to >= 0 && to < 64) {
                int toFile = to % 8;
                
                // Check file wrap
                if (Math.abs(dir) != 8 && Math.abs(fromFile - toFile) > 2) break;
                
                moves |= 1L << to;
                if ((occ & (1L << to)) != 0) break;
                
                to += dir;
                fromFile = toFile;
            }
        }
        return moves;
    }
    
    private Move createCastlingMove(boolean kingside) throws IllegalMoveException {
        Move move = new Move();
        move.piece = new Piece(Type.KING, sideToMove);
        
        if (sideToMove == Color.WHITE) {
            if (kingside && !wkCastle) throw new IllegalMoveException("White cannot castle kingside");
            if (!kingside && !wqCastle) throw new IllegalMoveException("White cannot castle queenside");
            move.target = kingside ? (byte) sqIdx("g1") : (byte) sqIdx("c1");
        } else {
            if (kingside && !bkCastle) throw new IllegalMoveException("Black cannot castle kingside");
            if (!kingside && !bqCastle) throw new IllegalMoveException("Black cannot castle queenside");
            move.target = kingside ? (byte) sqIdx("g8") : (byte) sqIdx("c8");
        }
        
        move.flags = kingside ? Move.FLAG_SHORT_CASTLE : Move.FLAG_LONG_CASTLE;
        return move;
    }
    
    private int getPieceIndex(Type type, Color color) {
        int offset = color == Color.WHITE ? 0 : 6;
        return type.ordinal() + offset;
    }
    
    private Type parseType(String s) {
        switch (s) {
            case "N": return Type.KNIGHT;
            case "B": return Type.BISHOP;
            case "R": return Type.ROOK;
            case "Q": return Type.QUEEN;
            case "K": return Type.KING;
            default: return Type.PAWN;
        }
    }
    
    /**
     * Execute a move on the board.
     * Usage: board.makeMove("e4") or board.makeMove(moveObject)
     */
    public void makeMove(String san) throws IllegalMoveException {
        Move move = parseSAN(san);
        makeMove(move);
    }
    
    /**
     * Move using coordinate notation (e.g., "e2 e4").
     * Converts to SAN and executes.
     */
    public void move(String from, String to) throws IllegalMoveException {
        int fromIdx = sqIdx(from);
        int toIdx = sqIdx(to);
        
        Piece piece = getPieceAt(fromIdx);
        if (piece == null) {
            throw new IllegalMoveException("No piece at " + from);
        }
        
        if (piece.color() != sideToMove) {
            throw new IllegalMoveException("Not your piece!");
        }
        
        // Build SAN string
        String san = buildSAN(fromIdx, toIdx, piece);
        makeMove(san);
    }
    
    private String buildSAN(int from, int to, Piece piece) {
        StringBuilder san = new StringBuilder();
        
        // Add piece prefix (except for pawns)
        if (piece.type() != Type.PAWN) {
            san.append(getPieceChar(piece.type()));
        }
        
        // Check if disambiguation is needed
        String disambig = getDisambiguation(from, to, piece);
        san.append(disambig);
        
        // Add capture marker
        int capturedIdx = pieceIndexAt(to);
        boolean isCapture = capturedIdx != -1;
        
        // En passant check
        if (piece.type() == Type.PAWN && to == enPassantSquare) {
            isCapture = true;
        }
        
        if (isCapture) {
            if (piece.type() == Type.PAWN && disambig.isEmpty()) {
                san.append((char)('a' + (from % 8))); // add source file for pawn captures
            }
            san.append('x');
        }
        
        // Add destination
        san.append(sqToStr(to));
        
        // Check for promotion (pawn reaching last rank)
        if (piece.type() == Type.PAWN) {
            int rank = to / 8;
            if (rank == 7 || rank == 0) {
                san.append("=Q"); // Default to queen
            }
        }
        
        return san.toString();
    }
    
    private String getDisambiguation(int from, int to, Piece piece) {
        if (piece.type() == Type.PAWN || piece.type() == Type.KING) {
            return "";
        }
        
        // Find other pieces of same type that can also move to 'to'
        List<Integer> candidates = new ArrayList<>();
        int pieceIdx = getPieceIndex(piece.type(), piece.color());
        long pieces = bb[pieceIdx];
        
        while (pieces != 0) {
            int sq = Long.numberOfTrailingZeros(pieces);
            pieces &= pieces - 1;
            
            if (sq != from && canMoveTo(sq, to, piece.type())) {
                candidates.add(sq);
            }
        }
        
        if (candidates.isEmpty()) {
            return "";
        }
        
        // Check if file is enough
        int fromFile = from % 8;
        boolean fileUnique = true;
        for (int cand : candidates) {
            if (cand % 8 == fromFile) {
                fileUnique = false;
                break;
            }
        }
        
        if (fileUnique) {
            return String.valueOf((char)('a' + fromFile));
        }
        
        // Check if rank is enough
        int fromRank = from / 8;
        boolean rankUnique = true;
        for (int cand : candidates) {
            if (cand / 8 == fromRank) {
                rankUnique = false;
                break;
            }
        }
        
        if (rankUnique) {
            return String.valueOf((char)('1' + fromRank));
        }
        
        // Need both file and rank
        return String.valueOf((char)('a' + fromFile)) + (char)('1' + fromRank);
    }
    
    private String sqToStr(int sq) {
        int file = sq % 8;
        int rank = sq / 8;
        return String.valueOf((char)('a' + file)) + (char)('1' + rank);
    }
    
    private char getPieceChar(Type type) {
        switch (type) {
            case KNIGHT: return 'N';
            case BISHOP: return 'B';
            case ROOK: return 'R';
            case QUEEN: return 'Q';
            case KING: return 'K';
            default: return ' ';
        }
    }
    
    public void makeMove(Move move) {
        int from = move.disambiguation;
        int to = move.target;
        int pieceIdx = getPieceIndex(move.piece.type(), move.piece.color());
        
        // Handle castling
        if ((move.flags & Move.FLAG_SHORT_CASTLE) != 0) {
            executeCastling(true);
            sideToMove = sideToMove == Color.WHITE ? Color.BLACK : Color.WHITE;
            return;
        }
        if ((move.flags & Move.FLAG_LONG_CASTLE) != 0) {
            executeCastling(false);
            sideToMove = sideToMove == Color.WHITE ? Color.BLACK : Color.WHITE;
            return;
        }
        
        // Remove piece from source
        bb[pieceIdx] &= ~(1L << from);
        
        // Handle en passant capture
        if ((move.flags & Move.FLAG_EN_PASSANT) != 0) {
            int captureSquare = sideToMove == Color.WHITE ? to - 8 : to + 8;
            int enemyPawnIdx = sideToMove == Color.WHITE ? BP : WP;
            bb[enemyPawnIdx] &= ~(1L << captureSquare);
        } else {
            // Remove captured piece (if any)
            int capturedIdx = pieceIndexAt(to);
            if (capturedIdx != -1) {
                bb[capturedIdx] &= ~(1L << to);
            }
        }
        
        // Handle promotion
        if ((move.flags & Move.FLAG_PROMOTION) != 0) {
            // Default to queen if not specified
            int promotionIdx = sideToMove == Color.WHITE ? WQ : BQ;
            bb[promotionIdx] |= 1L << to;
        } else {
            // Place piece at destination
            bb[pieceIdx] |= 1L << to;
        }
        
        // Update en passant square
        if (move.piece.type() == Type.PAWN && Math.abs(to - from) == 16) {
            enPassantSquare = sideToMove == Color.WHITE ? from + 8 : from - 8;
        } else {
            enPassantSquare = -1;
        }
        
        // Update castling rights
        updateCastlingRights(from, to, move.piece);
        
        // Switch side to move
        sideToMove = sideToMove == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
    
    private void executeCastling(boolean kingside) {
        if (sideToMove == Color.WHITE) {
            if (kingside) {
                // King e1 -> g1, Rook h1 -> f1
                bb[WK] = 0x0000000000000040L;
                bb[WR] &= ~0x0000000000000081L;
                bb[WR] |= 0x0000000000000020L;
            } else {
                // King e1 -> c1, Rook a1 -> d1
                bb[WK] = 0x0000000000000004L;
                bb[WR] &= ~0x0000000000000081L;
                bb[WR] |= 0x0000000000000008L;
            }
            wkCastle = wqCastle = false;
        } else {
            if (kingside) {
                // King e8 -> g8, Rook h8 -> f8
                bb[BK] = 0x4000000000000000L;
                bb[BR] &= ~0x8100000000000000L;
                bb[BR] |= 0x2000000000000000L;
            } else {
                // King e8 -> c8, Rook a8 -> d8
                bb[BK] = 0x0400000000000000L;
                bb[BR] &= ~0x8100000000000000L;
                bb[BR] |= 0x0800000000000000L;
            }
            bkCastle = bqCastle = false;
        }
    }
    
    private void updateCastlingRights(int from, int to, Piece piece) {
        // King moved - lose both castling rights
        if (piece.type() == Type.KING) {
            if (piece.color() == Color.WHITE) {
                wkCastle = wqCastle = false;
            } else {
                bkCastle = bqCastle = false;
            }
        }
        
        // Rook moved or captured - lose that side
        if (piece.type() == Type.ROOK || to == 0 || to == 7 || to == 56 || to == 63) {
            if (from == 0 || to == 0) wqCastle = false;
            if (from == 7 || to == 7) wkCastle = false;
            if (from == 56 || to == 56) bqCastle = false;
            if (from == 63 || to == 63) bkCastle = false;
        }
    }
    
    /**
     * Display the board in ASCII format (useful for debugging).
     */
    public void display() {
        System.out.println("  a b c d e f g h");
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int sq = rank * 8 + file;
                Piece p = getPieceAt(sq);
                if (p == null) {
                    System.out.print(". ");
                } else {
                    char symbol = getPieceSymbol(p);
                    System.out.print(symbol + " ");
                }
            }
            System.out.println();
        }
        System.out.println("\n" + sideToMove + " to move");
    }
    
    private char getPieceSymbol(Piece p) {
        char base = ' ';
        switch (p.type()) {
            case PAWN: base = 'P'; break;
            case KNIGHT: base = 'N'; break;
            case BISHOP: base = 'B'; break;
            case ROOK: base = 'R'; break;
            case QUEEN: base = 'Q'; break;
            case KING: base = 'K'; break;
        }
        return p.color() == Color.WHITE ? base : Character.toLowerCase(base);
    }
}

class IllegalMoveException extends Exception {
    public IllegalMoveException(String message) {
        super(message);
    }
}