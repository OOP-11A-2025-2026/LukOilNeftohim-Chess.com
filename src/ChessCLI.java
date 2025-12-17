import java.util.Scanner;
import model.*;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class ChessCLI {
    private static Board board;
    private static Scanner scanner;
    private static boolean gameActive;

    static Ansi.Color whitePiece = Ansi.Color.WHITE; 
    static Ansi.Color blackPiece = Ansi.Color.BLACK; 

    public static void main(String[] args) {
        AnsiConsole.systemInstall(); // Стартира Jansi
        scanner = new Scanner(System.in);
        gameActive = false;

        while (true) {
            showMainMenu();

            switch (getUserChoice()) {
                case 1 -> startNewGame();
                case 2 -> loadGame();
                case 3 -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    AnsiConsole.systemUninstall(); // Спира Jansi
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showMainMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      CHESS GAME - Main Menu           ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. New Game                          ║");
        System.out.println("║  2. Load Game from PGN                ║");
        System.out.println("║  3. Exit                              ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter choice (1-3): ");
    }

    private static void startNewGame() {
        board = new Board();
        gameActive = true;
        playGame();
    }

    private static void loadGame() {
        System.out.print("Enter PGN file name: ");
        String fileName = scanner.nextLine().trim();
        System.out.println("PGN loading not yet implemented.");
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void playGame() {
        while (gameActive) {
            clearScreen();
            printColoredBoard();

            String sideToMove = board.sideToMove == Color.WHITE ? "White" : "Black";
            System.out.println("\n" + sideToMove + "'s turn");
            System.out.println("Options: [move] [resign] [draw] [help]");
            System.out.print("Enter move: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("resign")) {
                handleResign();
                break;
            } else if (input.equalsIgnoreCase("draw")) {
                handleDraw();
                break;
            } else if (input.equalsIgnoreCase("help")) {
                showHelpMenu();
            } else if (!input.isEmpty()) {
                processMove(input);
            }
        }
    }

    private static void processMove(String input) {
        try {
            String[] parts = input.split("\\s+");

            if (parts.length == 1) {
                board.makeMove(input);
                System.out.println("✓ Move played: " + input);
            } else if (parts.length == 2) {
                String from = parts[0].toLowerCase();
                String to = parts[1].toLowerCase();

                board.move(from, to);
                System.out.println("✓ Move played: " + from + " → " + to);
            } else {
                System.out.println("Invalid input format. Use: e2 e4 or Nf3");
            }
        } catch (RuntimeException e) {
            System.out.println("✗ Illegal move: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }

        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void handleResign() {
        String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
        String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
        System.out.println(loser + " resigns. " + winner + " wins!");
        gameActive = false;
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void handleDraw() {
        System.out.print("Propose a draw? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.startsWith("y")) {
            System.out.println("The game ended in a draw.");
            gameActive = false;
        }
    }

    private static void showHelpMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      HELP - How to play               ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  Algebraic Notation (SAN):            ║");
        System.out.println("║    e4      - Pawn to e4               ║");
        System.out.println("║    Nf3     - Knight to f3             ║");
        System.out.println("║    Bxe5    - Bishop takes on e5       ║");
        System.out.println("║    O-O     - Kingside castling        ║");
        System.out.println("║    O-O-O   - Queenside castling       ║");
        System.out.println("║    e8=Q    - Pawn promotes to Queen   ║");
        System.out.println("║                                       ║");
        System.out.println("║  Coordinate Notation:                 ║");
        System.out.println("║    e2 e4   - Move from e2 to e4       ║");
        System.out.println("║    g1 f3   - Move from g1 to f3       ║");
        System.out.println("║                                       ║");
        System.out.println("║  Commands:                            ║");
        System.out.println("║    resign  - Give up the game         ║");
        System.out.println("║    draw    - Propose a draw           ║");
        System.out.println("║    help    - Show this help menu      ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Press Enter to return...");
        scanner.nextLine();
    }

    private static int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void clearScreen() {
        System.out.print(Ansi.ansi().eraseScreen().cursor(0, 0));
    }

    private static void printColoredBoard() {
        System.out.println("\n   a  b  c  d  e  f  g  h");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                boolean isLightSquare = (rank + file) % 2 == 0;
                int r = isLightSquare ? 200 : 90;
                int g = isLightSquare ? 190 : 67;
                int b = isLightSquare ? 180 : 33;
                
                Piece piece = board.getPieceAt(rank * 8 + file);
                String pieceChar = getPieceUnicode(piece);
                Ansi.Color fgColor = (piece != null && piece.color() == Color.WHITE) ? whitePiece : blackPiece;

                System.out.print(Ansi.ansi().bgRgb(r, g, b).fg(fgColor).bold().a(" " + pieceChar + " ").boldOff().reset());
            }
            System.out.println(" " + (rank + 1));
        }

        System.out.println("   a  b  c  d  e  f  g  h\n");
        String sideToMove = board.sideToMove == Color.WHITE ? "White" : "Black";
        System.out.println("Current Player: " + sideToMove);
    }

    private static String getPieceUnicode(Piece piece) {
        if (piece == null) return " ";

        return switch (piece.type()) {
            case KING -> "♚";
            case QUEEN -> "♛";
            case ROOK -> "♜";
            case BISHOP -> "♝";
            case KNIGHT -> "♞";
            case PAWN -> "♟";
        };
    }
}
