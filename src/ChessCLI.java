import java.util.Scanner;

public class ChessCLI {
    private static Board board;
    private static Scanner scanner;
    private static boolean gameActive;

    private static final String RESET = "\033[0m";
    private static final String WHITE_BG = "\033[47m";
    private static final String BLACK_BG = "\033[40m";
    private static final String WHITE_PIECE = "\033[97m";
    private static final String BLACK_PIECE = "\033[30m"; 

    private static final String[] UNICODE_PIECES = {
            "♔", "♕", "♖", "♗", "♘", "♙",
            "♚", "♛", "♜", "♝", "♞", "♟"
    };

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        gameActive = false;

        while (true) {
            showMainMenu();
    
            switch (getUserChoice()) {
                case 1 -> startNewGame();
                case 2 -> loadGame();
                case 3 -> 
                    {System.out.println("Goodbye!");
                    scanner.close();
                    return;}
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
        // TODO: Integrate PGN loading
        System.out.println("PGN loading not yet implemented.");
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void playGame() {
        while (gameActive) {
            clearScreen();
            printColoredBoard();

            String sideToMove = board.sideToMove == Color.WHITE ? "White" : "Black";
            System.out.println("\n" + sideToMove == Color.WHITE + "'s turn");
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
                // TODO: Implement algebraic notation parser

                System.out.println("Algebraic notation not yet fully implemented.");
                System.out.println("Please use coordinate notation: e2 e4");
            } else if (parts.length == 2) {
                String from = parts[0].toLowerCase();
                String to = parts[1].toLowerCase();

                board.move(from, to);
                
            } else {
                System.out.println("Invalid input format. Use: e2 e4 or Nf3");
            }
        } catch (Exception e) {
            System.out.println("Error processing move: " + e.getMessage());
        }

        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static void handleResign() {
        String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
        String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
        System.out.println(loser + " resigns. " + winner + " wins!");
        gameActive = false;
    }

    private static void handleDraw() {
        System.out.print("Propose a draw? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.toLowerCase().charAt(0) == 'y') {
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

    private static void clearScreen() 
    {
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
                
                Piece piece = board.getPieceAt(rank*8 + file); 
                String pieceType = getPieceUnicode(piece);
                String pieceColor = (piece != null && piece.color() == Color.WHITE) ? WHITE_PIECE : BLACK_PIECE;
                
                System.out.print(bgColor + pieceColor + " " + pieceType + " " + RESET);
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
            case KING   -> piece.color() == Color.WHITE ? "♔" : "♚";
            case QUEEN  -> piece.color() == Color.WHITE ? "♕" : "♛";
            case ROOK   -> piece.color() == Color.WHITE ? "♖" : "♜";
            case BISHOP -> piece.color() == Color.WHITE ? "♗" : "♝";
            case KNIGHT -> piece.color() == Color.WHITE ? "♘" : "♞";
            case PAWN   -> piece.color() == Color.WHITE ? "♙" : "♟";
        };
    }
}