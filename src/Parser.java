import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser за PNG/PGN формат. 
 *
 * 1) parsePNG(text):
 *    - Парсва всички PGN тагове като Key-Value
 *    - Парсва списъка от ходове (SAN нотация)
 *    - Всеки отделен ход се предава към parseMove(token)
 *
 * 2) parseMove(token):
 *    - Създава обект Move
 *    - Извиква под-методите за парсване в правилния ред:
 *        parsePiece
 *        parseCheckOrMate
 *        parsePromotion (евентуално извиква parsePiece)
 *        parseTarget
 *        parseCapture
 *        parseDisambiguation
 *
 * SAN има форма:
 *   [Piece] [Disambiguation] [x] [target] [=Promotion] [+/#]
 *
 * Парсването винаги става отзад напред, освен парсването на фигурата.
 *
 * Всеки под-метод попълва съответните полета в Move.
 * Методите използват extended switch за ясно и компактно разпознаване.
 */
public class Parser {

    public void parsePNG(String text) {
        Map<String, String> tags = parseTags(text);

        int lastTag = text.lastIndexOf(']');
        if (lastTag == -1) {
            return;
        }

        String movesBlock = text.substring(lastTag + 1);
        List<Move> moves = parseMoves(movesBlock);
    }

    private Map<String, String> parseTags(String text) {
        Map<String, String> Tags = new HashMap<>();

        Pattern TagPattern = Pattern.compile("\"\\\\[(\\\\w+)\\\\s+\\\"([^\\\"]*)\\\"\\\\]\"");
        Matcher TagMatcher = TagPattern.matcher(text);

        while(TagMatcher.find())
        {
            Tags.put(TagMatcher.group(1), TagMatcher.group(2));
        }

        return Tags;
    }


    private List<Move> parseMoves(String text) {
        List<Move> Moves = new ArrayList<>();

//        if (text.endsWith("1/2-1/2"))
//        {
//            //draw
//        } else if (text.endsWith("1-0")) {
//            //white
//        } else if (text.endsWith("0-1")) {
//            //black
//        }

        text = text.replaceAll("\\{[^}]*}", ""); // maha komentari
        //text = text.replaceAll("1-0|0-1|1/2-1/2|\\*", "");
        text = text.replaceAll("\\d+\\.", ""); // maha da
        System.out.println(text);

        String[] tokens = text.trim().split("\\s+");
        for (int i=0; i < tokens.length; i++)
        {
            System.out.println(tokens[i]);
            if (i%2 == 0) {
                Moves.add(parseMove(tokens[i], Color.WHITE));
            } else {
                Moves.add(parseMove(tokens[i], Color.BLACK));
            }
        }

        return Moves;
    }

    private Piece parsePiece(char pieceChar, Color c)
    {
        Piece p;
        switch (pieceChar)
        {
            case 'K': {
                p = new Piece(Type.KING, c);
                break;
            }
            case 'Q': {
                p = new Piece(Type.QUEEN, c);
                break;
            }
            case 'B': {
                p = new Piece(Type.BISHOP, c);
                break;
            }
            case 'N': {
                p = new Piece(Type.KNIGHT, c);
                break;
            }
            case 'R': {
                p = new Piece(Type.ROOK, c);
                break;
            }
            default: {
                p = new Piece(Type.PAWN, c);
                break;
            }
        }
        return p;
    }

    private byte convertPosToByte(char file, char rank) {
        return (byte) ( (rank - '1') * 8 + (file - 'a') );
    }


    private Move parseMove(String token, Color c) {
        Move move = new Move();

        if (token.equals("1-0") || token.equals("0-1") || token.equals("1/2-1/2")) {
            System.out.println(token);
            return move;
        }

        // castle
        if (token.equals("O-O-O")) {
            move.flags = (byte) (move.flags | Move.FLAG_LONG_CASTLE);
            return move;
        }
        else if (token.equals("O-O")) {
            move.flags = (byte) (move.flags | Move.FLAG_SHORT_CASTLE);
            return move;
        }


        if (token.contains("+")) {
            move.flags = (byte) (move.flags | Move.FLAG_CHECK);
            token = token.replace("+", "");
        }
        else if (token.contains("#")) {
            move.flags = (byte) (move.flags | Move.FLAG_MATE);
            token = token.replace("#", "");
        }


        Piece p;
        p = parsePiece(token.charAt(0), c);
        move.piece = p;

        token = token.substring(1);


        if (token.contains("=")) {
            move.flags = (byte) (move.flags | Move.FLAG_PROMOTION);

            p = parsePiece(token.charAt(token.indexOf("=")), c);
            move.piece = p;

            token = token.replace("=", "");
        }

        switch (token.length())
        {
            case 4:
            {
                //e4
                move.disambiguation = convertPosToByte(token.charAt(0), token.charAt(1));
                token = token.substring(2);
                break;
            }
            case 3:
            {
                //ami posle
                token = token.substring(1);
                break;
            }
            case 2: {break;}

            default: {
                //kaboom
                break;
            }
        }

        move.target = convertPosToByte(token.charAt(0), token.charAt(1));

        return move;
    }
}
