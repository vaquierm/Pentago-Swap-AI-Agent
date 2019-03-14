package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class MonteCarloTreeSearch {

    private static MonteCarloTreeNode currentRoot = null;
    private static boolean firstMove = true;

    public static void initNewGame(PentagoBoardState boardState) {
        currentRoot = new MonteCarloTreeNode();

        for (PentagoMove move : boardState.getAllLegalMoves()) {
            new MonteCarloTreeNode(currentRoot, move);
        }

        firstMove = true;
    }

    public static PentagoMove findBestMove(long timeout, PentagoBoardState boardState) {

        long startTime = System.currentTimeMillis();

        int maxPlayer = boardState.getTurnPlayer();


        while (System.currentTimeMillis() - startTime < timeout) {

            MonteCarloTreeNode nodeToExpand = findPromisingNodeWithULT();

            // TODO: Roll out with default policy and back propagate

        }

        return null;
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

        MonteCarloTreeNode maxNode = null;
        for (MonteCarloTreeNode child : root.getChildren()) {
            if (maxNode == null) {
                maxNode = traverseUCT(child);
                continue;
            }

            // Traverse
            MonteCarloTreeNode bestBelowChild = traverseUCT(child);

            // If better, update
            if (maxNode.getUCTValue() > bestBelowChild.getUCTValue()) {
                maxNode = bestBelowChild;
            }
        }

        return maxNode;
    }

}
