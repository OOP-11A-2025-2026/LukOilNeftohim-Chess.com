public class Main {
    public static void main(String[] args) {
        Board board = new Board();

        board.set("Qb5");
        board.set("Pa2");
        board.set("Nc6");
        board.set("Re1");
        board.set("Nf7");

        board.print();
        System.out.println("\n");

        board.pieceSees('N', "b1");
        System.out.println("\n");

        board.pieceSees('Q', "d4");
        System.out.println("\n");

        System.out.println("Total material: " + board.material());
    }
}