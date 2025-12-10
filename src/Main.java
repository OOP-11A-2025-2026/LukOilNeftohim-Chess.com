public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.set("Pc3");
        board.set("Rb1");
        board.print();

        System.out.println();
        board.move("c3", "c4");
        board.print();
    }
}