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

        MonteCarloTreeSearch.resetTree(customPentagoBoardState);

        while (System.currentTimeMillis() - startTime < timeout) {

            counter++;

            MonteCarloTreeNode nodeToExpand = MonteCarloTreeSearch.findPromisingNodeWithULT();

            CustomPentagoBoardState tempBoard = (CustomPentagoBoardState) customPentagoBoardState.clone();

            MonteCarloTreeSearch.applyMovesUpTo(tempBoard, nodeToExpand);

            MonteCarloTreeSearch.expandNode(nodeToExpand, tempBoard);

            MonteCarloTreeSearch.simulateDefaultPolicyAndBackPropagate(tempBoard, nodeToExpand, player);

        }

        System.out.println(counter + " default policy runs were ran in " + timeout + "ms");

        return MonteCarloTreeSearch.getBestMoveWithTree();

    }


    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<PentagoBoardState> constructor = PentagoBoardState.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        PentagoMove move;

        while (true) {

            PentagoBoardState boardState = constructor.newInstance();

            while (!boardState.gameOver()) {

                move = (PentagoMove) boardState.getRandomMove();
                boardState.processMove(move);

                move = findBestMoveMontecarlo(2000, (PentagoBoardState) boardState.clone());

                boardState.processMove(move);

            }

            boardState.printBoard();

            System.out.println("The winning player is " + boardState.getWinner());

        }
    }

}
