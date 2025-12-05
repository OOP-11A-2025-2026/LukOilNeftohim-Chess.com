public class Move{
    public Piece piece;         
    public byte target;          
    public boolean capture;     
    public Piece promotion;     
    public boolean check;
    public boolean mate;
    public boolean enPassant;
    public boolean castling;
    public class Disambiguation{
        public byte file;
        public byte rank;
    };
    
    public Move() {};
}