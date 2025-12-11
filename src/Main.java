public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.set("Pc3");
        board.set("Rb1");
        board.set("Nh1");
        board.print();

        System.out.println();
        board.move("c3", "c5");
        board.move("h1", "g3");
        board.print();
    }
}