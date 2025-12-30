package model;

import java.util.Random;

public class MagicBitboards {
    private static final int[] ROOK_BITS = {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };

    private static final int[] BISHOP_BITS = {
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };

    private final long[][] rookTable;
    private final long[][] bishopTable;
    private final long[] rookMasks;
    private final long[] bishopMasks;
    private final long[] rookMagics;
    private final long[] bishopMagics;

    public MagicBitboards() {
        rookMasks = new long[64];
        bishopMasks = new long[64];
        rookMagics = new long[64];
        bishopMagics = new long[64];
        
        rookTable = new long[64][];
        bishopTable = new long[64][];

        initMasks();
        initMagics();
        initTables();
    }

    private void initMasks() {
        for (int sq = 0; sq < 64; sq++) {
            rookMasks[sq] = generateRookMask(sq);
            bishopMasks[sq] = generateBishopMask(sq);
        }
    }

    private long generateRookMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank + 1; r < 7; r++) mask |= 1L << (r * 8 + file);
        for (int r = rank - 1; r > 0; r--) mask |= 1L << (r * 8 + file);
        for (int f = file + 1; f < 7; f++) mask |= 1L << (rank * 8 + f);
        for (int f = file - 1; f > 0; f--) mask |= 1L << (rank * 8 + f);

        return mask;
    }

    private long generateBishopMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        for (int r = rank + 1, f = file + 1; r < 7 && f < 7; r++, f++)
            mask |= 1L << (r * 8 + f);
        for (int r = rank + 1, f = file - 1; r < 7 && f > 0; r++, f--)
            mask |= 1L << (r * 8 + f);
        for (int r = rank - 1, f = file + 1; r > 0 && f < 7; r--, f++)
            mask |= 1L << (r * 8 + f);
        for (int r = rank - 1, f = file - 1; r > 0 && f > 0; r--, f--)
            mask |= 1L << (r * 8 + f);

        return mask;
    }

    private void initMagics() {
        // Pre-computed magic numbers for rooks
        long[] rookMagicNumbers = {
            0x0080001020400080L, 0x0040001000200040L, 0x0080081000200080L, 0x0080040800100080L,
            0x0080020400080080L, 0x0080010200040080L, 0x0080008001000200L, 0x0080002040800100L,
            0x0000800020400080L, 0x0000400020005000L, 0x0000801000200080L, 0x0000800800100080L,
            0x0000800400080080L, 0x0000800200040080L, 0x0000800100020080L, 0x0000800040800100L,
            0x0000208000400080L, 0x0000404000201000L, 0x0000808010002000L, 0x0000808008001000L,
            0x0000808004000800L, 0x0000808002000400L, 0x0000010100020004L, 0x0000020000408104L,
            0x0000208080004000L, 0x0000200040005000L, 0x0000100080200080L, 0x0000080080100080L,
            0x0000040080080080L, 0x0000020080040080L, 0x0000010080800200L, 0x0000800080004100L,
            0x0000204000800080L, 0x0000200040401000L, 0x0000100080802000L, 0x0000080080801000L,
            0x0000040080800800L, 0x0000020080800400L, 0x0000020001010004L, 0x0000800040800100L,
            0x0000204000808000L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000010002008080L, 0x0000004081020004L,
            0x0000204000800080L, 0x0000200040008080L, 0x0000100020008080L, 0x0000080010008080L,
            0x0000040008008080L, 0x0000020004008080L, 0x0000800100020080L, 0x0000800041000080L,
            0x00FFFCDDFCED714AL, 0x007FFCDDFCED714AL, 0x003FFFCDFFD88096L, 0x0000040810002101L,
            0x0001000204080011L, 0x0001000204000801L, 0x0001000082000401L, 0x0001FFFAABFAD1A2L
        };

        // Pre-computed magic numbers for bishops
        long[] bishopMagicNumbers = {
            0x0002020202020200L, 0x0002020202020000L, 0x0004010202000000L, 0x0004040080000000L,
            0x0001104000000000L, 0x0000821040000000L, 0x0000410410400000L, 0x0000104104104000L,
            0x0000040404040400L, 0x0000020202020200L, 0x0000040102020000L, 0x0000040400800000L,
            0x0000011040000000L, 0x0000008210400000L, 0x0000004104104000L, 0x0000002082082000L,
            0x0004000808080800L, 0x0002000404040400L, 0x0001000202020200L, 0x0000800802004000L,
            0x0000800400A00000L, 0x0000200100884000L, 0x0000400082082000L, 0x0000200041041000L,
            0x0002080010101000L, 0x0001040008080800L, 0x0000208004010400L, 0x0000404004010200L,
            0x0000840000802000L, 0x0000404002011000L, 0x0000808001041000L, 0x0000404000820800L,
            0x0001041000202000L, 0x0000820800101000L, 0x0000104400080800L, 0x0000020080080080L,
            0x0000404040040100L, 0x0000808100020100L, 0x0001010100020800L, 0x0000808080010400L,
            0x0000820820004000L, 0x0000410410002000L, 0x0000082088001000L, 0x0000002011000800L,
            0x0000080100400400L, 0x0001010101000200L, 0x0002020202000400L, 0x0001010101000200L,
            0x0000410410400000L, 0x0000208208200000L, 0x0000002084100000L, 0x0000000020880000L,
            0x0000001002020000L, 0x0000040408020000L, 0x0004040404040000L, 0x0002020202020000L,
            0x0000104104104000L, 0x0000002082082000L, 0x0000000020841000L, 0x0000000000208800L,
            0x0000000010020200L, 0x0000000404080200L, 0x0000040404040400L, 0x0002020202020200L
        };

        System.arraycopy(rookMagicNumbers, 0, rookMagics, 0, 64);
        System.arraycopy(bishopMagicNumbers, 0, bishopMagics, 0, 64);
    }

    private void initTables() {
        for (int sq = 0; sq < 64; sq++) {
            int rookSize = 1 << ROOK_BITS[sq];
            rookTable[sq] = new long[rookSize];
            initRookTable(sq);

            int bishopSize = 1 << BISHOP_BITS[sq];
            bishopTable[sq] = new long[bishopSize];
            initBishopTable(sq);
        }
    }

    private void initRookTable(int square) {
        long mask = rookMasks[square];
        int bits = ROOK_BITS[square];
        int permutations = 1 << bits;

        for (int i = 0; i < permutations; i++) {
            long occupancy = getOccupancyVariation(i, mask);
            int index = magicIndex(occupancy, rookMagics[square], bits);
            rookTable[square][index] = generateRookAttacks(square, occupancy);
        }
    }

    private void initBishopTable(int square) {
        long mask = bishopMasks[square];
        int bits = BISHOP_BITS[square];
        int permutations = 1 << bits;

        for (int i = 0; i < permutations; i++) {
            long occupancy = getOccupancyVariation(i, mask);
            int index = magicIndex(occupancy, bishopMagics[square], bits);
            bishopTable[square][index] = generateBishopAttacks(square, occupancy);
        }
    }

    private long getOccupancyVariation(int index, long mask) {
        long occupancy = 0L;
        int bitCount = Long.bitCount(mask);
        
        for (int i = 0; i < bitCount; i++) {
            int bitPos = Long.numberOfTrailingZeros(mask);
            mask &= mask - 1; // Clear the least significant bit
            
            if ((index & (1 << i)) != 0) {
                occupancy |= 1L << bitPos;
            }
        }
        
        return occupancy;
    }

    private long generateRookAttacks(int square, long occupancy) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // North
        for (int r = rank + 1; r < 8; r++) {
            attacks |= 1L << (r * 8 + file);
            if ((occupancy & (1L << (r * 8 + file))) != 0) break;
        }
        // South
        for (int r = rank - 1; r >= 0; r--) {
            attacks |= 1L << (r * 8 + file);
            if ((occupancy & (1L << (r * 8 + file))) != 0) break;
        }
        // East
        for (int f = file + 1; f < 8; f++) {
            attacks |= 1L << (rank * 8 + f);
            if ((occupancy & (1L << (rank * 8 + f))) != 0) break;
        }
        // West
        for (int f = file - 1; f >= 0; f--) {
            attacks |= 1L << (rank * 8 + f);
            if ((occupancy & (1L << (rank * 8 + f))) != 0) break;
        }

        return attacks;
    }

    private long generateBishopAttacks(int square, long occupancy) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Northeast
        for (int r = rank + 1, f = file + 1; r < 8 && f < 8; r++, f++) {
            attacks |= 1L << (r * 8 + f);
            if ((occupancy & (1L << (r * 8 + f))) != 0) break;
        }
        // Northwest
        for (int r = rank + 1, f = file - 1; r < 8 && f >= 0; r++, f--) {
            attacks |= 1L << (r * 8 + f);
            if ((occupancy & (1L << (r * 8 + f))) != 0) break;
        }
        // Southeast
        for (int r = rank - 1, f = file + 1; r >= 0 && f < 8; r--, f++) {
            attacks |= 1L << (r * 8 + f);
            if ((occupancy & (1L << (r * 8 + f))) != 0) break;
        }
        // Southwest
        for (int r = rank - 1, f = file - 1; r >= 0 && f >= 0; r--, f--) {
            attacks |= 1L << (r * 8 + f);
            if ((occupancy & (1L << (r * 8 + f))) != 0) break;
        }

        return attacks;
    }

    private int magicIndex(long occupancy, long magic, int bits) {
        return (int)((occupancy * magic) >>> (64 - bits));
    }

    public long getRookAttacks(int square, long occupancy) {
        occupancy &= rookMasks[square];
        int index = magicIndex(occupancy, rookMagics[square], ROOK_BITS[square]);
        return rookTable[square][index];
    }

    public long getBishopAttacks(int square, long occupancy) {
        occupancy &= bishopMasks[square];
        int index = magicIndex(occupancy, bishopMagics[square], BISHOP_BITS[square]);
        return bishopTable[square][index];
    }
}
