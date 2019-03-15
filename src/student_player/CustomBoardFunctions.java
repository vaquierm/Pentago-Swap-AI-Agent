package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class CustomBoardFunctions {

    // Reflection to avoid cloning to help revert a move to optimize memory
    private static Field quadrantsField;
    private static Method buildBoardFromQuadrantsMethod;
    private static Field turnPlayerField;
    private static Field turnNumberField;

    // Some static variables of the board state class
    private static int FIRST_PLAYER;
    private static int QUAD_SIZE;

    private static HashMap<Quadrant, Integer> quadToInt;

    static {
        try {
            quadrantsField = PentagoBoardState.class.getDeclaredField("quadrants");
            buildBoardFromQuadrantsMethod = PentagoBoardState.class.getDeclaredMethod("buildBoardFromQuadrants");
            turnPlayerField = PentagoBoardState.class.getDeclaredField("turnPlayer");
            turnNumberField = PentagoBoardState.class.getDeclaredField("turnNumber");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        quadrantsField.setAccessible(true);
        buildBoardFromQuadrantsMethod.setAccessible(true);
        turnPlayerField.setAccessible(true);
        turnNumberField.setAccessible(true);

        quadToInt = new HashMap<>(4);
        quadToInt.put(Quadrant.TL, 0);
        quadToInt.put(Quadrant.TR, 1);
        quadToInt.put(Quadrant.BL, 2);
        quadToInt.put(Quadrant.BR, 3);
    }

    /**
     * Initialization method for when a new game starts
     */
    public static void initNewGame(PentagoBoardState boardState) throws NoSuchFieldException, IllegalAccessException {
        Field f = boardState.getClass().getDeclaredField("QUAD_SIZE");
        f.setAccessible(true);
        QUAD_SIZE = (int) f.get(null);

        f = boardState.getClass().getDeclaredField("FIRST_PLAYER");
        f.setAccessible(true);
        FIRST_PLAYER = (int) f.get(null);
    }

    /**
     * Reverts a move made by the board.
     * Note: It is assumed that the move passed was actually made.
     * @param boardState  The board after the move was played
     * @param move  The last move that was played that needs to be reverted
     */
    public static void revertMove(PentagoBoardState boardState, PentagoMove move) {

        Piece[][][] quadrants = null;

        try {
            quadrants = (Piece[][][]) quadrantsField.get(boardState);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //Swapping mechanism
        int a = quadToInt.get(move.getASwap());
        int b = quadToInt.get(move.getBSwap());
        Piece[][] tmp = quadrants[a];
        quadrants[a] = quadrants[b];
        quadrants[b] = tmp;

        // Remove the piece placed
        int x = move.getMoveCoord().getX();
        int y = move.getMoveCoord().getY();
        boolean isLeftQuadMove = y / QUAD_SIZE == 0;
        boolean isTopQuadMove = x / QUAD_SIZE == 0;
        if (isLeftQuadMove && isTopQuadMove) { //Top left quadrant
            quadrants[0][x][y] = Piece.EMPTY;
        } else if (!isLeftQuadMove && isTopQuadMove) { //Top right quadrant
            quadrants[1][x][y % QUAD_SIZE] = Piece.EMPTY;
        } else if (isLeftQuadMove) { //Bottom left quadrant
            quadrants[2][x % QUAD_SIZE][y] = Piece.EMPTY;
        } else { //Bottom right quadrant
            quadrants[3][x % QUAD_SIZE][y % QUAD_SIZE] = Piece.EMPTY;
        }

        // Build board from quadrants
        try {
            buildBoardFromQuadrantsMethod.invoke(boardState);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // Reset the player to the previous player and the turn to the previous turn
        int turnPlayer = boardState.getTurnPlayer() == 1 ? 0 : 1;

        try {

            turnPlayerField.setInt(boardState, turnPlayer);

            if (turnPlayer != FIRST_PLAYER) {
                // Update back the turn number if necessary
                turnNumberField.setInt(boardState, boardState.getTurnNumber() - 1);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static boolean movesEqual(PentagoMove move1, PentagoMove move2) {
        return move1.getASwap() == move2.getASwap() &&
                move1.getBSwap() == move2.getBSwap() &&
                move1.getMoveCoord().getX() == move2.getMoveCoord().getX() &&
                move1.getMoveCoord().getY() == move2.getMoveCoord().getY();

    }

















    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {

        Constructor<PentagoBoardState> constructor = PentagoBoardState.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        PentagoBoardState b1 = constructor.newInstance();
        CustomPentagoBoardState b2 = new CustomPentagoBoardState();
        PentagoMove move;

        int numIter = (int)(Math.random() * 10);
        for (int i = 0; i < numIter && !b1.gameOver() ; i++) {
            move = (PentagoMove) b1.getRandomMove();
            b1.processMove(move);
            b2.processMove(move);
            if(!b2.boardEquals(b1)) {
                System.out.println("Boards are not equal");
            }
        }
        move = (PentagoMove) b1.getRandomMove();
        b2.processMove(move);
        b2.revertMove(move);

        if (!b2.boardEquals(b1)) {
            System.out.println("Revert not working");
            b1.printBoard();
            b2.printBoard();
        }


        PentagoBoardState boardState = constructor.newInstance();

        initNewGame(boardState);




        for (int i = 0; i < numIter && !boardState.gameOver() ; i++) {
            move = (PentagoMove) boardState.getRandomMove();
            boardState.processMove(move);
        }

        move = (PentagoMove) boardState.getRandomMove();

        PentagoBoardState beforeProcess = (PentagoBoardState) boardState.clone();


        boardState.processMove(move);

        CustomBoardFunctions.revertMove(boardState, move);

        System.out.println("Check if before and after look identical");

        System.out.println(beforeProcess.toString());

        System.out.println(boardState.toString());

        beforeProcess = null;
        boardState = null;

        System.out.println("Benchmark what runs faster");

        boardState = constructor.newInstance();
        PentagoBoardState clone = null;

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i ++) {
            if (boardState.gameOver()) {
                boardState = constructor.newInstance();
            }
            else {
                move = (PentagoMove) boardState.getRandomMove();
                boardState.processMove(move);
                clone = (PentagoBoardState) boardState.clone();
            }
        }
        System.out.println("Cloning took : " + (System.currentTimeMillis() - start) + "ms");

        boardState = constructor.newInstance();
        initNewGame(boardState);
        move = (PentagoMove) boardState.getRandomMove();
        boardState.processMove(move);
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i ++) {
            move = (PentagoMove) boardState.getRandomMove();
            boardState.processMove(move);
            revertMove(boardState, move);
        }
        System.out.println("Reverting took : " + (System.currentTimeMillis() - start) + "ms");
    }


}
