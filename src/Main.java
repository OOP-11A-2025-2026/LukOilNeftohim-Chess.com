import java.util.Scanner;

public class Main {
    private static Board board;
    private static Scanner scanner;
    private static boolean gameActive;

    // ANSI Color codes
    private static final String RESET = "\033[0m";
    private static final String WHITE_BG = "\033[47m";
    private static final String BLACK_BG = "\033[40m";
    private static final String WHITE_PIECE = "\033[97m"; // Bright white
    private static final String BLACK_PIECE = "\033[30m"; // Black
    private static final String HIGHLIGHT = "\033[43m"; // Yellow background

    // Unicode pieces
    private static final String[] UNICODE_PIECES = {
            "♔", "♕", "♖", "♗", "♘", "♙", // White pieces
            "♚", "♛", "♜", "♝", "♞", "♟"  // Black pieces
    };

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        gameActive = false;

        while (true) {
            showMainMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    startNewGame();
                    break;
                case 2:
                    loadGame();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
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
        board.loadDefault();
        gameActive = true;
        playGame();
    }

    private static void loadGame() {
        System.out.print("Enter PGN file name: ");
        String fileName = scanner.nextLine().trim();
        // TODO: Implement PGN loading
        System.out.println("PGN loading not yet implemented.");
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void playGame() {
        while (gameActive) {
            clearScreen();
            printColoredBoard();

            String currentPlayer = getCurrentPlayer();
            System.out.println("\n" + currentPlayer + "'s turn");
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
            // Parse algebraic notation or coordinate notation
            String[] parts = input.split("\\s+");

            if (parts.length == 1) {
                // Try algebraic notation: Nf3, e4, etc.
                // TODO: Implement algebraic notation parser
                System.out.println("Algebraic notation not yet fully implemented.");
                System.out.println("Please use coordinate notation: e2 e4");
            } else if (parts.length == 2) {
                // Coordinate notation: e2 e4
                String from = parts[0].toLowerCase();
                String to = parts[1].toLowerCase();

                if (isValidSquare(from) && isValidSquare(to)) {
                    board.move(from, to);
                } else {
                    System.out.println("Invalid square notation. Use format: e2 e4");
                }
            } else {
                System.out.println("Invalid input format. Use: e2 e4 or Nf3");
            }
        } catch (Exception e) {
            System.out.println("Error processing move: " + e.getMessage());
        }

        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static boolean isValidSquare(String square) {
        if (square.length() != 2) return false;
        char file = square.charAt(0);
        char rank = square.charAt(1);
        return file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8';
    }

    private static void handleResign() {
        boolean isWhiteToMove = true; // TODO: Get actual turn from board
        String loser = isWhiteToMove ? "White" : "Black";
        String winner = isWhiteToMove ? "Black" : "White";
        System.out.println(loser + " resigns. " + winner + " wins!");
        gameActive = false;
    }

    private static void handleDraw() {
        System.out.print("Propose a draw? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("y")) {
            System.out.println("The game ended in a draw.");
            gameActive = false;
        }
    }

    private static void showHelpMenu() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      HELP - How to play               ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  Coordinate Notation:  e2 e4          ║");
        System.out.println("║    (from square to square)            ║");
        System.out.println("║                                       ║");
        System.out.println("║  Algebraic Notation:   Nf3, e4        ║");
        System.out.println("║    (coming soon)                      ║");
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
        // Clear screen for better UX (works on most terminals)
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void printColoredBoard() {
        System.out.println("\n" + "   a  b  c  d  e  f  g  h");
        
        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            
            for (int file = 0; file < 8; file++) {
                boolean isLightSquare = (rank + file) % 2 == 0;
                String bgColor = isLightSquare ? WHITE_BG : BLACK_BG;
                
                char piece = getPieceCharAtSquare(file, rank);
                String pieceDisplay = getPieceUnicode(piece);
                String pieceColor = Character.isUpperCase(piece) ? WHITE_PIECE : BLACK_PIECE;
                
                System.out.print(bgColor + pieceColor + " " + pieceDisplay + " " + RESET);
            }
            
            System.out.println(" " + (rank + 1));
        }
        
        System.out.println("   a  b  c  d  e  f  g  h\n");
        
        // Display game status
        String currentPlayer = getCurrentPlayer();
        System.out.println("Current Player: " + currentPlayer);
    }

    private static char getPieceCharAtSquare(int file, int rank) {
        char fileLetter = (char) ('a' + file);
        char rankNumber = (char) ('1' + rank);
        String square = "" + fileLetter + rankNumber;
        
        // Access board's getPieceAt through reflection or add public method to Board
        try {
            java.lang.reflect.Method method = Board.class.getDeclaredMethod("getPieceAt", String.class);
            method.setAccessible(true);
            return (char) method.invoke(board, square);
        } catch (Exception e) {
            return '.';
        }
    }

    private static String getPieceUnicode(char piece) {
        return switch (piece) {
            case 'K' -> "♔";
            case 'Q' -> "♕";
            case 'R' -> "♖";
            case 'B' -> "♗";
            case 'N' -> "♘";
            case 'P' -> "♙";
            case 'k' -> "♚";
            case 'q' -> "♛";
            case 'r' -> "♜";
            case 'b' -> "♝";
            case 'n' -> "♞";
            case 'p' -> "♟";
            default -> "·";
        };
    }

    private static String getCurrentPlayer() {
        try {
            java.lang.reflect.Field field = Board.class.getDeclaredField("whiteToMove");
            field.setAccessible(true);
            boolean whiteToMove = field.getBoolean(board);
            return whiteToMove ? "⚪ White" : "⚫ Black";
        } catch (Exception e) {
            return "Unknown";
        }
    }
}