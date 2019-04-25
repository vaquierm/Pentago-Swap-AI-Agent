package Thanos_Mode_110.Montecarlo;

import Thanos_Mode_110.PentagoBitBoard;
import Thanos_Mode_110.PentagoBitMove;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static Thanos_Mode_110.PentagoBitBoard.DRAW;

/**
 * Class representing an Upper Confidence Tree (UCT) for use in Monte Carlo Tree search.
 * Note that in order to save on memory, only moves are store in each node. To get the current state at a given node
 * Moves are applied form the root to the current node to generate this state
 */
class UCTNode {

    private static final ThreadLocalRandom rand = ThreadLocalRandom.current();

    private int winScore;
    private int numSims;

    private final long move;
    private final byte player;

    private UCTNode parent;
    private List<UCTNode> children;

    private static final double EXPLOITATION_PARAM = Math.sqrt(2);

    public UCTNode(long move, UCTNode parent) {
        this.move = move;
        this.player = (byte) PentagoBitMove.getPlayer(move);

        this.winScore = 0;
        this.numSims = 0;

        this.parent = parent;

        this.children = null;
    }

    /**
     * Backpropagates the result of a default policy simulation back up to the root node. Note a win increments win
     * score by 2, a draw increments by 1 and a loss increments by 0. Each simulation increments numSims by 2. (such
     * that it doesn't appear that the win/sim ratio is twice what it is supposed to be
     *
     * @param winner the result of the default policy
     */
    void backPropagate(int winner) {

        UCTNode currentNode = this;
        // Continue to root
        while(currentNode != null) {
            // Increment the number of simulations
            currentNode.numSims += 2;

            if (winner == DRAW)
                currentNode.winScore++;
            else if (player == winner)
                currentNode.winScore += 2;

            // Move to parent
            currentNode = currentNode.parent;
        }
    }

    /**
     * Calculates the value of this state given it's win score, the number of simulations and the number of simulations
     * of it's parent
     * @return this state's value
     */
    double getUCTValue() {

        if (this.numSims == 0)
            return Double.MAX_VALUE;

        return (this.winScore / (double) this.numSims) + EXPLOITATION_PARAM * Math.sqrt(Math.log(this.parent.numSims)/this.numSims);
    }


    /**
     * Generates a PentagoBitBoard state by applying moves from root to this node. (design choice to minimize memory
     * usage)
     * @param startState the current state of the game (state at the root).
     * @return the state at this node
     */
    PentagoBitBoard getState(PentagoBitBoard startState) {

        // If we are at the root, game state is unchanged
        if(this.move == 0) return startState;

        // Get the chain of moves from the parent to this move
        Stack<Long> moveStack = new Stack<>();
        UCTNode currentNode = this;
        while(currentNode != null && currentNode.move != 0) {
            moveStack.push(currentNode.move);
            currentNode = currentNode.parent;
        }

        // Apply the moves
        PentagoBitBoard endState = (PentagoBitBoard) startState.clone();
        while(!moveStack.isEmpty()) {
            endState.processMove(moveStack.pop());
        }

        return endState;
    }

    void setParent(UCTNode parent) {
        this.parent = parent;
    }

    boolean hasChildren() {
        return !(this.children == null || this.children.size() == 0);
    }

    List<UCTNode> getChildren() {
        return children;
    }

    long getMove() {
        return move;
    }

    double getWinRate() {
        if (numSims == 0) {
            return 0;
        }

        return (double) winScore / (double) numSims;
    }

    double getWinScore() {
        return winScore;
    }

    int getNumSims() {
        return numSims;
    }

    void expandNode(List<Long> moves) {

        children = new ArrayList<>(moves.size());

        for (Long move : moves) {

            children.add(new UCTNode(move, this));

        }

    }

    UCTNode getChildMaxUCTValue() {
        return Collections.max(children, Comparator.comparing(UCTNode::getUCTValue));
    }

    UCTNode getRandomChild() {
        return children.get(rand.nextInt(children.size()));
    }
}