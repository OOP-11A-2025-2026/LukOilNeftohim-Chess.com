package model;
import java.util.*;


public class Game {
    private Board board;
    private List<Move> moves;
    private Map<String, String> tags;

    public Game() {
        this.board = new Board();
        this.moves = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public Board getBoard() { return board; }
    public List<Move> getMoves() { return moves; }
    public Map<String, String> getTags() { return tags; }

    public void addMove(String input) throws Board.IllegalMoveException {
        board.move(input);

        Move last = new Move();
        last.piece = board.getPieceAt(board.lastMoveTo);
        last.target = board.lastMoveTo;
        last.disambiguation = board.lastMoveFrom;
        last.flags = 0;

        moves.add(last);
    }
}
