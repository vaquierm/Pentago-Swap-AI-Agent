package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Agent {

    /**
     * Find best move with a monte carlo tree search with UCT
     * @param timeout  Allowed search time
     * @param boardState  Current board state
     * @return  The best move to play
     */
    public static PentagoMove findBestMoveMontecarlo(long timeout, PentagoBoardState boardState) {

        long startTime = System.currentTimeMillis();

        CustomPentagoBoardState customPentagoBoardState = new CustomPentagoBoardState(boardState);

        int player = boardState.getTurnPlayer();

        int counter = 0;

        PentagoMove winMove = MonteCarloTreeSearch.resetTree(customPentagoBoardState);

        if (winMove != null) {
            return winMove;
        }

        while (System.currentTimeMillis() - startTime < timeout) {

            counter++;

            MonteCarloTreeNode nodeToExpand = MonteCarloTreeSearch.findPromisingNodeWithULT();

            CustomPentagoBoardState tempBoard = (CustomPentagoBoardState) customPentagoBoardState.clone();

            MonteCarloTreeSearch.applyMovesUpTo(tempBoard, nodeToExpand);

            MonteCarloTreeSearch.expandNode(nodeToExpand, tempBoard, player);

            MonteCarloTreeSearch.simulateDefaultPolicyAndBackPropagate(tempBoard, nodeToExpand, player);

        }

        System.out.println(counter + " default policy runs were ran in " + timeout + "ms");

        return MonteCarloTreeSearch.getBestMoveWithTree(player, customPentagoBoardState);

    }


    /**
     * Finds the best move with minimax and alpha beta pruning
     * @param timeout  The time given to explore
     * @param boardState  The board state
     * @return  The best move to play
     */
    public static PentagoMove findBestMoveMiniMax(long timeout, PentagoBoardState boardState) {

        long startTime = System.currentTimeMillis();

        return MiniMax.computeMove(new CustomPentagoBoardState(boardState));
    }


    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<PentagoBoardState> constructor = PentagoBoardState.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        PentagoMove move;

        while (true) {

            PentagoBoardState boardState = constructor.newInstance();

            long start;

            while (!boardState.gameOver()) {

                System.out.println("It's player " + boardState.getTurnPlayer() + "'s turn.");

                start = System.currentTimeMillis();

                move = findBestMoveMontecarlo(1000, (PentagoBoardState) boardState.clone());

                System.out.println((System.currentTimeMillis() - start) + "ms to play");

                boardState.processMove(move);

                boardState.printBoard();

                System.out.println("Board has value: " + (new CustomPentagoBoardState(boardState)).evaluate());

                System.out.println("It's player " + boardState.getTurnPlayer() + "'s turn.");

                start = System.currentTimeMillis();

                move = findBestMoveMiniMax(1000, (PentagoBoardState) boardState.clone());

                System.out.println((System.currentTimeMillis() - start) + "ms to play");

                boardState.processMove(move);

                boardState.printBoard();

                System.out.println("Board has value: " + (new CustomPentagoBoardState(boardState)).evaluate());

            }

            boardState.printBoard();

            System.out.println("The winning player is " + boardState.getWinner());

        }
    }

}
