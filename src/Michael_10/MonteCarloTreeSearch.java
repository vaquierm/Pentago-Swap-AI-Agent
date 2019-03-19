package Michael_10;

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
            if (!moveLeadsToLoss(move, boardState, boardState.getTurnPlayer()))
                new MonteCarloTreeNode(root, move);
        }
    }

    /**
     * Get the current best move to play
     * @return  The best move to play according to the tree
     */
    public static PentagoMove getBestMoveWithTree(int player, CustomPentagoBoardState boardState) {

        MonteCarloTreeNode max = Collections.max(root.getChildren(), Comparator.comparing(MonteCarloTreeNode::getWinRatio));


        if (max.getWinRatio() > 1000) {
            // If you can win, win
            System.out.println("Win move!");
            return max.getMove();
        }

        boolean clear = false;

        if (boardState.getTurnNumber() < 3) {
            // If the game has not started that much yet no need to worry
            System.out.println("No need to check move since it is before move 3");
            clear = true;
        }

        while (!clear) {
            List<MonteCarloTreeNode> unexploredChildren = max.getUnexploredChildren();

            boolean loss = false;


            if (unexploredChildren.size() == 0) {
                clear = true;
                System.out.println("No unexplored children");

                if (max.hasChildWithLossStatus()) {
                    // If the move has a child with a loss status it is unsafe, revert the clear status and indicate that it leads to a loss
                    System.out.println("Move has a child with a loss status this is unsafe");
                    clear = false;
                    loss = true;
                }
            }

            // Process the move to see what the board would be like if we played the move max
            boardState.processMove(max.getMove());

            for (MonteCarloTreeNode unexploredChild : unexploredChildren) {

                System.out.println("There are " + unexploredChildren.size() + " child moves unexplored");

                if (moveLeadsToLoss(unexploredChild.getMove(), boardState, player)) {
                    // The move leads to a winning position for the opponent
                    loss = true;
                    break;
                }
            }

            if (loss) {
                // A move leading to a win for the opponent was found in the children.
                // We should not make this move. Get a new max
                root.getChildren().remove(max);

                if (root.getChildren().size() == 0) {
                    // If none of the moves are clear, we cannot win, return the move anyways
                    return max.getMove();
                }

                max = Collections.max(root.getChildren(), Comparator.comparing(MonteCarloTreeNode::getWinRatio));

                System.out.println("The move was unsafe, getting new move");
            } else {
                // There were no losses found for the opponent
                // The move is clear
                clear = true;

                System.out.println("The move was safe");
            }
        }

        return max.getMove();
    }

    /**
     * Check if a particular move leads to a loss for the player
     * Note: The boars state will be reverted back to normal
     * @param move  The move to be played
     * @param boardState  The current board state
     * @param player  The player to check for
     * @return  If the move leads to a loss
     */
    public static boolean moveLeadsToLoss(PentagoMove move, CustomPentagoBoardState boardState, int player) {

        boolean returnVal;

        boardState.processMove(move);

        if (boardState.getWinner() == 1 - player) {
            returnVal = true;
        } else {
            returnVal = false;
        }

        boardState.revertMove(move);

        return returnVal;
    }

    /**
     * Back propagate the result value from the node
     * @param node  Node to back propagate from
     * @param value  Value to back propagate
     */
    public static void backPropagateFromNode(MonteCarloTreeNode node, int value) {

        while (node != null) {
            node.visit();
            if (value != 0)
                node.incrementWinCount(value); //TODO: Need to figure out the loss
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
        int count = 0;
        boolean opponentStart = boardState.getTurnPlayer() == 1 - player;

        while (!boardState.gameOver()) {

            count++;

            randomMove = (PentagoMove) boardState.getRandomMove();

            boardState.processMove(randomMove);
        }

        if (boardState.getWinner() == player) {
            value += 1;
        }
        else if (boardState.getWinner() == 1 - player) {
            value -= 1;
            if (opponentStart && count == 1) {
                // If the opponent was able to win in one move, we should not consider this move
                node.updateStatus(MonteCarloTreeNode.Status.LOSS);
            }
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
