package model;

public enum Type {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

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
}

