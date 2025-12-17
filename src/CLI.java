import java.util.Scanner;
import model.*;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class CLI {
    private static Board board;
    private static Scanner scanner;
    private static boolean gameActive;

    static Ansi.Color whitePiece = Ansi.Color.WHITE; 
    static Ansi.Color blackPiece = Ansi.Color.BLACK; 

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
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
                    AnsiConsole.systemUninstall();
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
        String errorMessage = null;
        
        while (gameActive) {
            clearScreen();
            printColoredBoard();

            // Показваме грешка ако има
            if (errorMessage != null) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("✗ " + errorMessage).reset());
                errorMessage = null;
            }
            
            System.out.println();

            System.out.print(board.sideToMove == Color.WHITE ? "White's turn: " : "Black's turn: ");
            
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("resign")) {
                handleResign();
                break;
            } else if (input.equalsIgnoreCase("draw")) {
                handleDraw();
                if (!gameActive) break;
            } else if (input.equalsIgnoreCase("undo")) {
                if (board.canUndo()) {
                    board.undo();
                } else {
                    errorMessage = "No moves to undo";
                }
            } else if (input.equalsIgnoreCase("help")) {
                showHelpMenu();
            } else if (!input.isEmpty()) {
                errorMessage = processMove(input);
            }
        }
    }

    private static String processMove(String input) {
        try {
            board.move(input);
            return null; // Няма грешка
        } catch (Board.IllegalMoveException e) {
            return e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static void handleResign() {
        String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
        String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
        System.out.println(loser + " resigns. " + winner + " wins!");
        gameActive = false;
        scanner.nextLine();
    }

    private static void handleDraw() {
        System.out.println("The game ended in a draw.");
        gameActive = false;
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
        System.out.println("║    Nbd7    - Knight from b-file to d7 ║");
        System.out.println("║    R1a3    - Rook from rank 1 to a3   ║");
        System.out.println("║                                       ║");
        System.out.println("║  Coordinate Notation:                 ║");
        System.out.println("║    e2 e4   - Move from e2 to e4       ║");
        System.out.println("║    g1 f3   - Move from g1 to f3       ║");
        System.out.println("║                                       ║");
        System.out.println("║  Commands:                            ║");
        System.out.println("║    undo    - Take back last move      ║");
        System.out.println("║    resign  - Give up the game         ║");
        System.out.println("║    draw    - End game in a draw       ║");
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
        // Показваме взетите фигури от черния играч (взети от белия)
        System.out.println();
        
        if (!board.capturedByBlack.isEmpty()) {
            System.out.print("Captured: ");
            for (Piece p : board.capturedByBlack) {
                System.out.print(Ansi.ansi().fg(Ansi.Color.WHITE).bold().a(getPieceUnicode(p) + " ").reset());
            }
        }
        
        System.out.println("\n   a  b  c  d  e  f  g  h");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                boolean isLightSquare = (rank + file) % 2 == 0;
                
                // Highlighting за последния ход
                boolean isFromSquare = square == board.lastMoveFrom;
                boolean isToSquare = square == board.lastMoveTo;
                
                int r, g, b;
                
                if (isFromSquare || isToSquare) {
                    // Жълто-зелен highlight за последния ход
                    r = 180;
                    g = 180;
                    b = 80;
                } else if (isLightSquare) {
                    r = 200;
                    g = 190;
                    b = 180;
                } else {
                    r = 90;
                    g = 67;
                    b = 33;
                }
                
                Piece piece = board.getPieceAt(square);
                String pieceChar = getPieceUnicode(piece);
                Ansi.Color fgColor = (piece != null && piece.color() == Color.WHITE) ? whitePiece : blackPiece;

                System.out.print(Ansi.ansi().bgRgb(r, g, b).fg(fgColor).bold().a(" " + pieceChar + " ").boldOff().reset());
            }
            System.out.println(" " + (rank + 1));
        }

        System.out.println("   a  b  c  d  e  f  g  h");
        
        // Показваме взетите фигури от белия играч (взети от черния)
        System.out.println();
        if (!board.capturedByWhite.isEmpty()) {
            System.out.print("Captured: ");
            for (Piece p : board.capturedByWhite) {
                System.out.print(Ansi.ansi().fg(Ansi.Color.BLACK).bold().a(getPieceUnicode(p) + " ").reset());
            }
            System.out.println();
        }
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