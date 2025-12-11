public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.loadDefault();
        try {
            board.move("a2", "a3");
            board.move("b7", "b5");
            board.move("a3", "a4");
            board.move("b5", "a4");
            board.move("a1", "a4");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } finally {
            board.print();
        }
    }
}