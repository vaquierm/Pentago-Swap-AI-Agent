package student_player;

import boardgame.Board;
import pentago_swap.PentagoMove;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class MonteCarloTreeSearch {

    private static MonteCarloTreeNode root;

    public static MonteCarloTreeNode getRoot() {
        return root;
    }

    /**
     * Resets the tree for a new move
     * @param boardState  The state of the board at start of move
     */
    public static void resetTree(CustomPentagoBoardState boardState) {
        root = new MonteCarloTreeNode();

        for (PentagoMove move : boardState.getAllLegalMovesWithSymmetry()) {
            new MonteCarloTreeNode(root, move);
        }
    }

    /**
     * Get the current best move to play
     * @return  The best move to play according to the tree
     */
    public static PentagoMove getBestMoveWithTree() {

        MonteCarloTreeNode max = Collections.max(root.getChildren(), Comparator.comparing(MonteCarloTreeNode::getWinRatio));
        MonteCarloTreeNode min = Collections.min(root.getChildren(), Comparator.comparing(MonteCarloTreeNode::getWinRatio));

        System.out.println("Max node has win rate: " + max.getWinRatio() + " for move " + max.getMove().getMoveCoord().getX() + ", " + max.getMove().getMoveCoord().getY());
        System.out.println("Min node has win rate: " + min.getWinRatio() + " for move " + max.getMove().getMoveCoord().getX() + ", " + max.getMove().getMoveCoord().getY());


        if (max.getWinRatio() > 1000) {
            // If you can win, win
            return max.getMove();
        }
        else if (min.getWinRatio() < -1000) {
            // If you need to block, block
            return min.getMove();
        }

        return max.getMove();
    }

    /**
     * Back propagate the result value from the node
     * @param node  Node to back propagate from
     * @param value  Value to back propagate
     */
    public static void backPropagateFromNode(MonteCarloTreeNode node, int value) {

        while (node.getParent() != null) {
            node.visit();
            if (value > 0)
                node.win(); //TODO: Need to figure out the loss
            node = node.getParent();
        }
    }

    /**
     * Select a promising node to run the default policy from
     * @return  The promising node
     */
    public static MonteCarloTreeNode findPromisingNodeWithULT() {
        MonteCarloTreeNode node = root;
        while (node.getChildren().size() != 0) {
            node = node.getChildWithBestUCT();
        }
        return node;
    }

    /**
     * Expands the node
     * @param node  Node to expand
     * @param boardState  Board state at this node
     */
    public static void expandNode(MonteCarloTreeNode node, CustomPentagoBoardState boardState, int player) {

        if (boardState.gameOver()) {
            // If game done, don't expand
            if (boardState.getWinner() == player) {
                node.updateStatus(MonteCarloTreeNode.Status.WON);
            }
            else if (boardState.getWinner() == 1 - player) {
                node.updateStatus(MonteCarloTreeNode.Status.LOSS);
            }
            return;
        }

        //TODO: only do by symmetry if necessary
        List<PentagoMove> possibleMoves = boardState.getAllLegalMovesWithSymmetry();

        for (PentagoMove move : possibleMoves) {
            new MonteCarloTreeNode(node, move);
        }
    }


    /**
     * Simulate a random play out from a specific node
     * Note: The board state will be modified
     * @param boardState  Board state from which the random play out starts
     * @param node  The node at which the random play out starts
     * @param player  The player that our agent is
     */
    public static void simulateDefaultPolicyAndBackPropagate(CustomPentagoBoardState boardState, MonteCarloTreeNode node, int player) {

        int value = 0;

        PentagoMove randomMove;
        while (!boardState.gameOver()) {

            randomMove = (PentagoMove) boardState.getRandomMove();

            boardState.processMove(randomMove);
        }

        if (boardState.getWinner() == player) {
            value += 1;
        }
        else if (boardState.getWinner() == 1 - player) {
            value -= 1;
        }

        backPropagateFromNode(node, value);
    }


    /**
     * From the initial board state which corresponds to the root, apply the moves up to this node
     * @param boardState  Board state at root
     * @param node  Node to apply moves to
     */
    public static void applyMovesUpTo(CustomPentagoBoardState boardState, MonteCarloTreeNode node) {
        LinkedList<PentagoMove> moves = new LinkedList<>();

        while(node.getParent() != null) {
            moves.addFirst(node.getMove());
            node = node.getParent();
        }

        for (PentagoMove move: moves) {
            boardState.processMove(move);
        }
    }

}
