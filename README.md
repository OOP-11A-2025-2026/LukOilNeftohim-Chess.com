# LukOilNeftohim-Chess.com
## Program Logic
### Piece

Вид и цвят на фигурата

 ```java
enum Type { //Вид на фигурата
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
enum Color { //Цвят на фигурата
    WHITE, BLACK;
```

- функция toSANChar

### Move

 ```java
    public static final byte FLAG_SHORT_CASTLE=  0b00000001;//двата вида рокада
    public static final byte FLAG_LONG_CASTLE =  0b00000010;
    public static final byte FLAG_EN_PASSANT  =  0b00000100;// ен пасант
    public static final byte FLAG_PROMOTION   =  0b00001000;//повишение на пешка
    public static final byte FLAG_CHECK       =  0b00010000;//шах
    public static final byte FLAG_MATE        =  0b00100000;//шах и мат

    public Piece piece;         
    public byte target;
    public byte flags;
    public byte disambiguation; 
```

- фигурата, която се мести

- мястото, на което да отиде

- byte disambiguation

### Game

Създава самата игра:

- board

- движенията

- tags

### Storage

- Клас за писане и четене във файл.
