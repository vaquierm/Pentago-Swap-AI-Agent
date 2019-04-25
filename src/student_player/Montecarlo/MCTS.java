package student_player.Montecarlo;

import student_player.PentagoBitBoard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements all logic for the MCTS algorithm.
 * An Upper confidence tree (UCT) is used to as a tree policy.
 * The default policy has been modified such that if a win move is available, it is played.
 * If not, then a random move is player. This has been made possible due to the fact that win moves can be
 * found in O(1) due to a clever implementation in the PentagoBitBoard class.
 */
public class MCTS {

    private static UCTNode UCTRoot;


    /**
     * Finds the best move to play using the MCST algorithm
     * @param timeout Time allocated to play move
     * @param moves Moves to chose from
     * @param boardState Current board state of the board
     * @return Best move to play according to the MCTS algorithm.
     */
    public static long getMCTSBestMove(long timeout, List<Long> moves, PentagoBitBoard boardState) {

        long startTime = System.currentTimeMillis();

        // Crate the root of the UCT tree
        UCTRoot = new UCTNode(0, null);

        // Expand the root node with the option moves
        UCTRoot.expandNode(moves);

        while (System.currentTimeMillis() - startTime < timeout && UCTRoot.getNumSims() < 500000) {

            // Find a promising node to expand
            UCTNode promissingNode = findPromisingNode();

            // Expand the node and run the default policy
            expandAndRunDefaultPolicy(promissingNode, boardState);
        }

        UCTNode bestNode = Collections.max(UCTRoot.getChildren(), Comparator.comparingDouble(UCTNode::getWinRate));

        System.out.println(UCTRoot.getNumSims() + " simulations were ran in " + timeout + " ms.");
        System.out.println("Returning best move with win rate: " + bestNode.getWinRate());

        return bestNode.getMove();
    }


    /**
     * Finds the best move to expand from the upper confidence tree
     * @return The node to expand next.
     */
    private static UCTNode findPromisingNode() {

        UCTNode promissingNode = UCTRoot;

        while (promissingNode.hasChildren()) {
            promissingNode = promissingNode.getChildMaxUCTValue();
        }

        return promissingNode;
    }


    /**
     * Expands the node in the UCT and runs the default policy from it.
     * The winner of the simulation is backpropagated through the tree.
     * Note: The default policy has been modified such that is a move that leads to a win is available, it is played
     * @param node Node to expand
     * @param rootState The state of the board at the root
     */
    private static void expandAndRunDefaultPolicy(UCTNode node, PentagoBitBoard rootState) {

        // Get the state of the node
        PentagoBitBoard nodeState = node.getState(rootState);

        if (nodeState.gameOver()) {
            // Get the moves that can be made from node
            List<Long> moves = nodeState.getAllLegalNonSymmetricMoves();

            if (moves.size() > 0) {
                // Expand the node
                node.expandNode(moves);

                node = node.getRandomChild();

                // Apply the move of the child that was chosen
                nodeState.processMove(node.getMove());
            }
        }

        while (!nodeState.gameOver()) {

            // Check if there is a win move
            long move = nodeState.getWinMove(nodeState.getTurnPlayer());

            // If there is a win move, play it
            if (nodeState.isLegalMove(move)) {
                nodeState.processMove(move);
            }
            else {
                // If not, play a random move
                nodeState.processMove(nodeState.getRandomMove());
            }

        }

        node.backPropagate(nodeState.getWinner());

    }

}
