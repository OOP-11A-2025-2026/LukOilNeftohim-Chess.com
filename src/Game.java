import java.io.*;
import java.util.*;

// =====================
// Клас за шахматна партия
// =====================
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
}
