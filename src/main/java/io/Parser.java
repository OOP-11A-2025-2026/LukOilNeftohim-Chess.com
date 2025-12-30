package io;
import java.util.*;
import java.util.regex.*;

import model.*;

public class Parser {

    
    public Game parseGame(String text) {
        Game game = new Game();

        game.getTags().putAll(parseTags(text));

        int lastTag = text.lastIndexOf(']');
        String movesBlock = (lastTag == -1) ? text : text.substring(lastTag + 1);

        List<Move> moves = parseMoves(movesBlock);
        game.getMoves().addAll(moves);

        return game;
    }

    
    public Move parseSingleMove(String token, Color color) {
        token = token.trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Empty move string");
        }
        return parseMove(token, color);
    }
    
    
    private Map<String, String> parseTags(String text) {
        Map<String, String> tags = new HashMap<>();

        Pattern p = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"\\]");
        Matcher m = p.matcher(text);

        while (m.find()) {
            tags.put(m.group(1), m.group(2));
        }

        return tags;
    }

    
    private List<Move> parseMoves(String text) {
        List<Move> moves = new ArrayList<>();

        text = text.replaceAll("\\{[^}]*\\}", ""); // comments
        text = text.replaceAll("\\d+\\.", "");     // move numbers
        text = text.replaceAll("\\s+", " ").trim();

        String[] tokens = text.split(" ");

        Color side = Color.WHITE;
        for (String token : tokens) {
            if (isResult(token)) break;
            Move move = parseMove(token, side);
            moves.add(move);
            side = side.opposite();
        }

        return moves;
    }

    
    private Move parseMove(String token, Color color) {
        Move move = new Move();
        move.disambiguation = -1;

        if (parseCastling(move, token)) {
            move.piece = new Piece(Type.KING, color);
            return move;
        }

        token = parseCheckOrMate(move, token);
        token = parsePromotion(move, token, color);
        token = parseTarget(move, token);
        token = parseCapture(move, token);
        token = parseDisambiguation(move, token);
        parsePiece(move, token, color);

        return move;
    }

    
    private boolean isResult(String token) {
        return token.equals("1-0") || token.equals("0-1") || token.equals("1/2-1/2") || token.equals("*");
    }

    
    private boolean parseCastling(Move move, String token) {
        switch (token.replaceAll("[+#]", "")) {
            case "O-O" -> {
                move.flags |= Move.FLAG_SHORT_CASTLE;
                return true;
            }
            case "O-O-O" -> {
                move.flags |= Move.FLAG_LONG_CASTLE;
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    
    private String parseCheckOrMate(Move move, String token) {
        if (token.isEmpty()) return token;
        
        char last = token.charAt(token.length() - 1);

        switch (last) {
            case '+' -> {
                move.flags |= Move.FLAG_CHECK;
                return token.substring(0, token.length() - 1);
            }
            case '#' -> {
                move.flags |= Move.FLAG_MATE;
                return token.substring(0, token.length() - 1);
            }
            default -> {
                return token;
            }
        }
    }

    
    private String parsePromotion(Move move, String token, Color color) {
        int idx = token.indexOf('=');
        if (idx == -1) return token;

        move.flags |= Move.FLAG_PROMOTION;

        if (idx + 1 < token.length()) {
            char p = token.charAt(idx + 1);
            Type promotionType = switch (p) {
                case 'Q' -> Type.QUEEN;
                case 'R' -> Type.ROOK;
                case 'B' -> Type.BISHOP;
                case 'N' -> Type.KNIGHT;
                default -> Type.QUEEN; // default to queen
            };
            
            move.piece = new Piece(promotionType, color);
        }

        return token.substring(0, idx);
    }

    
    private String parseTarget(Move move, String token) {
        if (token.length() < 2) {
            throw new IllegalArgumentException("Invalid move: " + token);
        }
        
        int len = token.length();

        char file = token.charAt(len - 2);
        char rank = token.charAt(len - 1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid target square: " + file + rank);
        }

        move.target = ((rank - '1') * 8 + (file - 'a'));

        return token.substring(0, len - 2);
    }

    
    private String parseCapture(Move move, String token) {
        if (!token.contains("x")) return token;

        move.flags |= Move.FLAG_CAPTURE;
        return token.replace("x", "");
    }

    
    private String parseDisambiguation(Move move, String token) {
        if (token.isEmpty()) return token;

        char c = token.charAt(token.length() - 1);

        if (c >= 'a' && c <= 'h') {
            move.disambiguation = (c - 'a');      // 0–7 (file)
            return token.substring(0, token.length() - 1);
        }

        if (c >= '1' && c <= '8') {
            move.disambiguation = (8 + (c - '1')); // 8–15 (rank)
            return token.substring(0, token.length() - 1);
        }

        return token;
    }

    
    private void parsePiece(Move move, String token, Color color) {
        char first = token.isEmpty() ? ' ' : token.charAt(0);

        Type type = switch (first) {
            case 'K' -> Type.KING;
            case 'Q' -> Type.QUEEN;
            case 'R' -> Type.ROOK;
            case 'B' -> Type.BISHOP;
            case 'N' -> Type.KNIGHT;
            default -> Type.PAWN;
        };

        if (move.piece == null) move.piece = new Piece(type, color);
        else move.piece = new Piece(type, move.piece.color());
    }
}
