package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class MonteCarloTreeSearch1 {

    private static MonteCarloTreeNode currentRoot = null;
    private static PentagoBoardState lastState = null;

    public static void initNewGame(PentagoBoardState boardState) {
        currentRoot = new MonteCarloTreeNode();

        CustomPentagoBoardState state = new CustomPentagoBoardState(boardState);

        for (PentagoMove move : state.getAllLegalMovesWithSymmetry()) {
            new MonteCarloTreeNode(currentRoot, move);
        }
    }

    public static PentagoMove findBestMove(long timeout, PentagoBoardState boardState) {

        long startTime = System.currentTimeMillis();

        int player = boardState.getTurnPlayer();

        findLastMoveAndUpdateRoot(boardState);

        int counter = 0;

        while (System.currentTimeMillis() - startTime < timeout) {

            counter++;

            MonteCarloTreeNode nodeToExpand = findPromisingNodeWithULT();

            PentagoBoardState tempBoard = (PentagoBoardState) boardState.clone();

            applyMovesUpTo(tempBoard, nodeToExpand);

            simulateDefaultPolicyAndBackPropagate(tempBoard, nodeToExpand, player);

        }

        System.out.println(counter + " default policy runs were ran in " + timeout + "ms");

        // Traverse the upper tree with minimax/alpha-beta and return the move that will lead to the best UCT score
        MonteCarloTreeNode bestNode = MiniMaxTools.computeBestMove(currentRoot);

        // Slice the rest of that tree that will no longer be needed and update the root
        currentRoot = bestNode;
        bestNode.removeParent();

        // Apply the move and save the state to figure out what state the AI plays later
        boardState.processMove(currentRoot.getMove());
        lastState = (PentagoBoardState) boardState.clone();



        return bestNode.getMove();
    }

    private static void findLastMoveAndUpdateRoot(PentagoBoardState boardState) {

        if (lastState == null) {
            initNewGame(boardState);
            return;
        }

        CustomPentagoBoardState state = new CustomPentagoBoardState(lastState);

        System.out.println("Saved " + currentRoot.getChildren().size() + " out of " + state.getAllLegalMoves().size());
        for (MonteCarloTreeNode node : currentRoot.getChildren()) {
            PentagoMove move = node.getMove();

            try {
                state.processMove(move);
            } catch (Exception e) {
                state.printBoard();
                e.printStackTrace();
            }

            if (state.boardEquals(boardState)) {
                // This is the move the opponent made. Update the root and get rid of the rest of the tree
                System.out.println("Found the play the AI made!");
                currentRoot = node;
                currentRoot.removeParent();
            }

            // Revert the move that was just made
            state.revertMove(move);
        }

        // If the move that the opponent made was never explored re initialize the tree like if it was a new game starting at the new board state
        initNewGame(boardState);
    }

    /**
     * Traverse the tree and return the node with the largestUCT value
     */
    private static MonteCarloTreeNode findPromisingNodeWithULT() {
        return traverseUCT(currentRoot);
    }


    private static MonteCarloTreeNode traverseUCT(MonteCarloTreeNode root)
    {
        if (root.isLeaf()) {
            return root;
        }

        MonteCarloTreeNode maxNode = root;
        for (MonteCarloTreeNode child : root.getChildren()) {

            // Traverse
            MonteCarloTreeNode bestBelowChild = traverseUCT(child);

            // If better, update
            if (maxNode.getUCTValue() < bestBelowChild.getUCTValue()) {
                maxNode = bestBelowChild;
            }
        }

        return maxNode;
    }

    private static void applyMovesUpTo(PentagoBoardState boardState, MonteCarloTreeNode node) {
        LinkedList<PentagoMove> moves = new LinkedList<>();

        while(node.getParent() != null) {
            moves.addFirst(node.getMove());
            node = node.getParent();
        }

        for (PentagoMove move: moves) {
            boardState.processMove(move);
        }
    }

    /**
     * Simulate a random play out from a specific node
     * Note: The board state will be modified
     * @param boardState  Board state from which the random play out starts
     * @param node  The node at which the random play out starts
     * @param player  The player that our agent is
     */
    private static void simulateDefaultPolicyAndBackPropagate(PentagoBoardState boardState, MonteCarloTreeNode node, int player) {

        MonteCarloTreeNode newNode = null;

        PentagoMove randomMove;
        while (!boardState.gameOver()) {

            randomMove = (PentagoMove) boardState.getRandomMove();

            if (newNode == null) {
                // See if the random move exist
                newNode = findNodeWithMove(node.getChildren(), randomMove);

                if (newNode != null) {
                    node = newNode;
                    newNode = null;
                }
                else {
                    newNode = new MonteCarloTreeNode(node, randomMove);
                }
            }

            boardState.processMove(randomMove);
        }

        int value = 0;
        if (boardState.getWinner() == player) {
            value = 1;
        }
        else if (boardState.getWinner() == 1 - player) {
            value = 0; // TODO: need to figure out what do do in case of a tie
        }

        if (newNode == null)
            newNode = node;

        backPropagateFromNode(newNode, value);
    }

    private static void backPropagateFromNode(MonteCarloTreeNode node, int value) {

        while (node.getParent() != null) {
            node.visit();
            if (value > 0)
                node.win();
            node = node.getParent();
        }
    }

    private static MonteCarloTreeNode findNodeWithMove(List<MonteCarloTreeNode> nodes, PentagoMove move) {
        for (MonteCarloTreeNode node : nodes) {
            if (CustomBoardFunctions.movesEqual(node.getMove(), move)) {
                return node;
            }
        }
        return null;
    }





    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<PentagoBoardState> constructor = PentagoBoardState.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        PentagoBoardState boardState = constructor.newInstance();

        PentagoMove move;

        while (!boardState.gameOver()) {

            move = findBestMove(2000, (PentagoBoardState) boardState.clone());

            boardState.processMove(move);

            move = (PentagoMove) boardState.getRandomMove();
            boardState.processMove(move);
        }

        System.out.println("The winning player is " + boardState.getWinner());


    }

}
