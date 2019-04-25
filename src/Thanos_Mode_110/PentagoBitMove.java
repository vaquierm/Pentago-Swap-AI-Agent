package Thanos_Mode_110;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.HashMap;

import static pentago_swap.PentagoBoardState.BOARD_SIZE;

public class PentagoBitMove {

    private static HashMap<Integer, PentagoBoardState.Quadrant> intToQuad;

    static {
        intToQuad = new HashMap<>(4);
        intToQuad.put(0, PentagoBoardState.Quadrant.TL);
        intToQuad.put(1, PentagoBoardState.Quadrant.TR);
        intToQuad.put(2, PentagoBoardState.Quadrant.BL);
        intToQuad.put(3, PentagoBoardState.Quadrant.BR);
    }

    /**
     * Create a BitMove Long integer.
     * @param player The player to play
     * @param quadrantA The first quadrant to swap
     * @param quadrantB The second quadrant to swap
     * @param col y
     * @param row x
     * @return The long representation of the move
     */
    public static long createBitMove(int player, int quadrantA, int quadrantB, int col, int row) {

        // Create a bitMove long int with the coordinates to play at
        long bitMove = colRowToBitCoord(col, row);

        // Add the player bit
        bitMove = setPlayer(bitMove, player);

        // Add the A and B quadrants to swap
        bitMove = setAQuad(bitMove, quadrantA);
        bitMove = setBQuad(bitMove, quadrantB);

        return  bitMove;
    }

    /**
     * Create a BitMove Long integer
     * @param player The play to play
     * @param quadrantA The first quadrant to swap
     * @param quadrantB The second quadrant to swap
     * @param bitMove The long representation of the coordinate to play
     * @return The long representation of the move
     */
    public static long createBitMove(int player, int quadrantA, int quadrantB, long bitMove) {

        // Add the player bit
        bitMove = setPlayer(bitMove, player);

        // Add the A and B quadrants to swap
        bitMove = setAQuad(bitMove, quadrantA);
        bitMove = setBQuad(bitMove, quadrantB);

        return  bitMove;
    }

    /**
     * Converts an col, row position to a placement in a bit move.
     *
     * @param col column position (from 0 to 5 inclusive)
     * @param row row position (from 0 to 5 inclusive)
     * @return long with placement bit at correct position
     */
    public static long colRowToBitCoord(int col, int row) {

        // Get col in correct position:
        long bit = (1 << (BOARD_SIZE - 1 - col));

        // Get row in correct position
        bit = bit << (BOARD_SIZE * (BOARD_SIZE - 1 - row));

        return bit;
    }

    /**
     * Extracts the column and row number placement of a move long.
     *
     * @param move The move to extract
     * @return integer array containing coordinates
     */
    public static int[] bitCoordToColRow(long move) {

        int bitPosition = 0;
        long mask = 1;
        while((move & mask) != mask) {
            bitPosition++;
            mask = mask << 1;
        }

        int col = (BOARD_SIZE*BOARD_SIZE - 1 - bitPosition) % BOARD_SIZE;
        int row = (BOARD_SIZE*BOARD_SIZE - 1 - bitPosition) / BOARD_SIZE;

        return new int[] {col, row};
    }

    public static int getPlayer(long move) {
        return (int) ((move >> 40) & 1);
    }

    public static long setPlayer(long move, int player) {
        return move | (((long) (player & 1)) << 40);
    }

    public static int getAQuad(long move) {
        return (int) ((move >> 38) & 0b11);
    }

    public static long setAQuad(long move, int AQuad) {
        return move | (((long) (AQuad & 0b11)) << 38);
    }

    public static int getBQuad(long move) {
        return (int) ((move >> 36) & 0b11);
    }

    public static long setBQuad(long move, int BQuad) {
        return move | (((long) (BQuad & 0b11)) << 36);
    }

    public static long getBitCoord(long move) {
        return move & 0b111111111111111111111111111111111111L;
    }

    public static long setPriority(long move, int priority) {
        return move | (((long) (priority & 0b11111)) << 42);
    }

    public static int getPriority(long move) {
        return (int) ((move >> 42) & 0b11111);
    }

    public static long getBitMoveNoPriority(long move) {
        return move & 0b11111111111111111111111111111111111111111L;
    }

    /**
     * Coverts a move long to a PentagoMove object
     * @param move The move long to convert
     * @return a new equivalent Pentago move
     */
    static PentagoMove bitMoveToPentagoMove(long move) {

        int player = getPlayer(move);
        int smallerQuad = getAQuad(move);
        int largerQuad = getBQuad(move);
        long coord = getBitCoord(move);

        int[] coordColRow = bitCoordToColRow(coord);

        // Different coordinate system so return row -> y, col -> x
        return new PentagoMove(coordColRow[1], coordColRow[0], PentagoBoardState.Quadrant.values()[smallerQuad], PentagoBoardState.Quadrant.values()[largerQuad], player);
    }

    public static String toPrettyString(long move) {
        int[] coord = bitCoordToColRow(move);
        return String.format("Player %d, Move: (%d, %d), Swap: (%s, %s)", getPlayer(move), coord[0], coord[1], intToQuad.get(getAQuad(move)), intToQuad.get(getBQuad(move)));
    }

}
