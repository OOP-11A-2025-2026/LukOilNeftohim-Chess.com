import java.util.Scanner;

import model.*;
import io.Storage;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class CLI {
    private static Game game;
    private static Board board;
    private static Scanner scanner;
    private static boolean gameActive;
    private static Timer timer;

    static int whiteR = 255;
    static int whiteG = 255;
    static int whiteB = 255;

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

    private static void startNewGame(){
        game = new Game();
        board = game.getBoard();
        
        // Choose time control
        timer = selectTimeControl();
        if (timer != null) timer.startTimer(Color.WHITE);
        

        gameActive = true;
        playGame();
    }

    private static void loadGame() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      Load Game from PGN               ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Enter PGN file name: ");
        String fileName = scanner.nextLine().trim();
    
        try {
            game = Storage.readGame(fileName);
            board = new Board(); 
    
            for (Move m : game.getMoves()) {
                board.resolveAndMakeMove(m);
            }
    
            System.out.println("\n✓ Game loaded successfully!");
            System.out.println("Event: " + game.getTags().getOrDefault("Event", "Unknown"));
            System.out.println("White: " + game.getTags().getOrDefault("White", "Unknown"));
            System.out.println("Black: " + game.getTags().getOrDefault("Black", "Unknown"));
            System.out.println("Moves loaded: " + game.getMoves().size());
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
    
            gameActive = true;
            playGame();
    
        } catch (Exception e) {
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED)
                .a("\n✗ Error loading game: " + e.getMessage()).reset());
            System.out.print("Press Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void playGame() {
        String errorMessage = null;
        
        while (gameActive) {
            clearScreen();
            printColoredBoard();

            if (errorMessage != null) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("✗ " + errorMessage).reset());
                errorMessage = null;
            }
            
            System.out.println();
            
            // Display timer if enabled
            if (timer != null) {
                String whiteTime = timer.getFormattedTime(Color.WHITE);
                String blackTime = timer.getFormattedTime(Color.BLACK);
                System.out.println("White: " + whiteTime + "  |  Black: " + blackTime);
                
                // Check for timeout
                if (timer.isTimeOut(board.sideToMove)) {
                    String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
                    String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
                    System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a("✗ " + loser + " is out of time! " + winner + " wins!").reset());
                    gameActive = false;
                    game.getTags().put("Result", board.sideToMove == Color.WHITE ? "0-1" : "1-0");
                    break;
                }
            }
            
            System.out.print(Ansi.ansi().bold().a(game.getBoard().sideToMove == Color.WHITE ? "White's turn: " : "Black's turn: ").reset());
            
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("resign")) {
                handleResign();
                break;
            } else if (input.equalsIgnoreCase("draw")) {
                errorMessage = handleDrawOffer();
                if (!gameActive) break;
            } else if (input.equalsIgnoreCase("undo")) {
                if (board.canUndo()) {
                    board.undo();
                } else {
                    errorMessage = "No moves to undo";
                }
            } else if (input.equalsIgnoreCase("save")) {
                saveGame();
            } else if (input.equalsIgnoreCase("help")) {
                showHelpMenu();
            } else if (!input.isEmpty()) {
                errorMessage = processMove(input);
            }
        }
    }

    private static String processMove(String input) {
        try {
            game.addMove(input);
            
            // Check for checkmate
            if (board.isCheckmate()) {
                String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
                String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
                System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).bold().a("\nCheckmate! " + winner + " wins!").reset());
                game.getTags().put("Result", board.sideToMove == Color.WHITE ? "0-1" : "1-0");
                gameActive = false;
                if (timer != null) timer.shutdown();
                return null;
            }
            
            // Check for stalemate
            if (board.isStalemate()) {
                System.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).bold().a("\nStalemate! The game is drawn.").reset());
                game.getTags().put("Result", "1/2-1/2");
                gameActive = false;
                if (timer != null) timer.shutdown();
                return null;
            }
            
            if (timer != null) {
                timer.stopTimer();
                timer.switchPlayer();
                timer.startTimer(board.sideToMove);
            }
            
            return null; 
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static void handleResign() {
        String loser = board.sideToMove == Color.WHITE ? "White" : "Black";
        String winner = board.sideToMove == Color.WHITE ? "Black" : "White";
        System.out.println(loser + " resigns. " + winner + " wins!");
        
        // Update result
        String result = board.sideToMove == Color.WHITE ? "0-1" : "1-0";
        game.getTags().put("Result", result);
        
        // Stop timer
        if (timer != null) timer.shutdown();
        
        
        gameActive = false;
        scanner.nextLine();
    }

    private static void handleDraw() {
        System.out.println("The game ended in a draw.");
        game.getTags().put("Result", "1/2-1/2");
        
        if (timer != null) timer.shutdown();

        gameActive = false;
    }
    
    private static String handleDrawOffer() {
        Color offeringPlayer = board.sideToMove;
        Color respondingPlayer = offeringPlayer.opposite();
        
        System.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(offeringPlayer + " offers a draw.").reset());
        System.out.print(Ansi.ansi().bold().a(respondingPlayer + ", do you accept the draw? (yes/no): ").reset());
        
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (response.equals("yes") || response.equals("y")) {
            System.out.println("Draw accepted! The game ended in a draw.");
            game.getTags().put("Result", "1/2-1/2");
            
            if (timer != null) timer.shutdown();
            
            gameActive = false;
            return null;
        } else {
            return "Draw offer rejected. Game continues.";
        }
    }
    
    private static void saveGame() {
        System.out.print("Enter filename to save (e.g., game.pgn): ");
        String fileName = scanner.nextLine().trim();
        
        if (fileName.isEmpty()) {
            fileName = "game_" + System.currentTimeMillis() + ".pgn";
        }
        
        if (!fileName.endsWith(".pgn")) {
            fileName += ".pgn";
        }
        
        try {                        
            Storage.writeGame(fileName, game);
            
            System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN)
                .a("✓ Game saved to " + fileName).reset());
            System.out.print("Press Enter to continue...");
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED)
                .a("✗ Error saving game: " + e.getMessage()).reset());
            System.out.print("Press Enter to continue...");
            scanner.nextLine();
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
        System.out.println("║    Nbd7    - Knight from b-file to d7 ║");
        System.out.println("║    R1a3    - Rook from rank 1 to a3   ║");
        System.out.println("║                                       ║");
        System.out.println("║  Coordinate Notation:                 ║");
        System.out.println("║    e2 e4   - Move from e2 to e4       ║");
        System.out.println("║    g1 f3   - Move from g1 to f3       ║");
        System.out.println("║                                       ║");
        System.out.println("║  Commands:                            ║");
        System.out.println("║    undo    - Take back last move      ║");
        System.out.println("║    save    - Save game to PGN file    ║");
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
        System.out.println();
        System.out.print("Black captured: ");
        if (board.capturedByBlack.isEmpty()) System.out.println("—");
        else {
            for (Piece p : board.capturedByBlack) {
                System.out.print(Ansi.ansi().fg(Ansi.Color.WHITE).bold().a(getPieceUnicode(p) + " ").reset());
            }
            System.out.println();
        }
        
        System.out.println("\n   a  b  c  d  e  f  g  h");

        for (int rank = 7; rank >= 0; rank--) {
            System.out.print((rank + 1) + " ");
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                boolean isLightSquare = (rank + file) % 2 == 1;
                
                boolean isFromSquare = square == board.lastMoveFrom;
                boolean isToSquare = square == board.lastMoveTo;
                
                int r, g, b;
                
                if (isFromSquare || isToSquare) {
                    r = 180;
                    g = 180;
                    b = 80;
                } else if (isLightSquare) {
                    r = 180;
                    g = 180;
                    b = 180;
                } else {
                    r = 90;
                    g = 67;
                    b = 33;
                }
                
                Piece piece = board.getPieceAt(square);
                String pieceChar = getPieceUnicode(piece);
                boolean isWhite = piece != null && piece.color() == Color.WHITE;

                if (piece != null) {
                    if (isWhite) {
                        System.out.print(Ansi.ansi().bgRgb(r, g, b).fgRgb(whiteR, whiteG, whiteB).bold().a(" " + pieceChar + " ").boldOff().reset());
                    } else {
                        System.out.print(Ansi.ansi().bgRgb(r, g, b).fg(blackPiece).bold().a(" " + pieceChar + " ").boldOff().reset());
                    }
                } else {
                    System.out.print(Ansi.ansi().bgRgb(r, g, b).a("   ").reset());
                }
            }
            System.out.println(" " + (rank + 1));
        }

        System.out.println("   a  b  c  d  e  f  g  h");
        
        System.out.println();
        System.out.print("White captured: ");
        if (board.capturedByWhite.isEmpty()) System.out.println("—");
        else {
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



    
    private static Timer selectTimeControl() {
        clearScreen();
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║      Select Time Control              ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║  1. Bullet (1 min + 0 sec increment)  ║");
        System.out.println("║  2. Blitz (3 min + 2 sec increment)   ║");
        System.out.println("║  3. Rapid (10 min + 0 sec increment)  ║");
        System.out.println("║  4. Classical (30 min)                ║");
        System.out.println("║  5. Custom time                       ║");
        System.out.println("║  6. No timer (unlimited)              ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.print("Choose option (1-6): ");
        
        int choice = getUserChoice();
        
        return switch (choice) {
            case 1 -> new Timer(60000, 0);           // 1 minute, no increment
            case 2 -> new Timer(180000, 2000);       // 3 minutes, 2 second increment
            case 3 -> new Timer(600000, 0);          // 10 minutes, no increment
            case 4 -> new Timer(1800000, 0);         // 30 minutes, no increment
            case 5 -> selectCustomTime();
            case 6 -> null;                          // No timer
            default -> new Timer(300000, 0);         // Default: 5 minutes
        };
    }
    
    private static Timer selectCustomTime() {
        System.out.print("Enter initial time in minutes: ");
        long minutes = 0;
        try {
            minutes = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            minutes = 5;
        }
        
        System.out.print("Enter increment in seconds (0 for none): ");
        long increment = 0;
        try {
            increment = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            increment = 0;
        }
        
        return new Timer(minutes * 60 * 1000, increment * 1000);
    }
}
