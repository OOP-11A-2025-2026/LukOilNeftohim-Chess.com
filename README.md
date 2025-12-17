# LukOilNeftohim-Chess.com
## Program Logic
### Piece

- enum за черни и бели фигури

- enum за вида на фигурата
 ```javascript I'm A tab
enum Type {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
```

- функция toSANChar

### Move

- флагове за:
- двата вида рокада, ен пасант, повишение на пешка, шах, шах и мат

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
