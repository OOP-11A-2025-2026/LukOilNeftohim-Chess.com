public enum Piece {
  PAWN,
  KNIGHT,
  BISHOP,
  ROOK,
  QUEEN,
  KING;

  public char toSANChar() {
      return switch (this) {
          case PAWN -> '\0';
          case KNIGHT -> 'N';
          case BISHOP -> 'B';
          case ROOK -> 'R';
          case QUEEN -> 'Q';
          case KING -> 'K';
      };
  }

  /**
   * Преобразува символ от SAN в Piece.
   * Fail-fast: хвърля IllegalArgumentException за невалидни символи.
   */
  public static Piece fromSANChar(char c) {
      return switch (c) {
          case 'N' -> KNIGHT;
          case 'B' -> BISHOP;
          case 'R' -> ROOK;
          case 'Q' -> QUEEN;
          case 'K' -> KING;
          default -> throw new IllegalArgumentException("Invalid SAN piece character: " + c);
      };
  }
}
