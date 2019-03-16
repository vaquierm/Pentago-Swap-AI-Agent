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

            MonteCarloTreeSearch.expandNode(nodeToExpand, tempBoard, player);

            MonteCarloTreeSearch.simulateDefaultPolicyAndBackPropagate(tempBoard, nodeToExpand, player);

        }

        System.out.println(counter + " default policy runs were ran in " + timeout + "ms");

        return MonteCarloTreeSearch.getBestMoveWithTree(player, customPentagoBoardState);

    }


    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<PentagoBoardState> constructor = PentagoBoardState.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        PentagoMove move;

        while (true) {

            PentagoBoardState boardState = constructor.newInstance();

            while (!boardState.gameOver()) {

                System.out.println("It's player " + boardState.getTurnPlayer() + "'s turn.");

                move = findBestMoveMontecarlo(500, (PentagoBoardState) boardState.clone());
                boardState.processMove(move);

                boardState.printBoard();

            }

            boardState.printBoard();

            System.out.println("The winning player is " + boardState.getWinner());

        }
    }

}
