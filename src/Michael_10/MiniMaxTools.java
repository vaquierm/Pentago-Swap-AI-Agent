package Michael_10;

import java.util.List;

public class MiniMaxTools {

    public static MonteCarloTreeNode computeBestMove(MonteCarloTreeNode root) {
        MonteCarloTreeNode bestNode = null;
        double bestVal = -100000;
        for (MonteCarloTreeNode child : root.getChildren()) {
            double val = minimax(child, 6, false, -100000, 100000);
            if (val > bestVal) {
                bestVal = val;
                bestNode = child;
            }
        }

        return bestNode;
    }

    private static double minimax(MonteCarloTreeNode node, int level, boolean max, double alpha, double beta) {
        if (node.isLeaf() || level == 0) {
            return node.getWinRatio();
        }

        List<MonteCarloTreeNode> children = node.getChildren();

        double score;

        if (max) {
            // Find max and store in alpha
            for (MonteCarloTreeNode child : children) {
                score = minimax(child, level - 1, false, alpha, beta);
                if (score > alpha) alpha = score;
                if (alpha >= beta) break;  // beta cut-off
            }
            return alpha;
        } else {
            // Find min and store in beta
            for (MonteCarloTreeNode child : children) {
                score = minimax(child, level - 1, true, alpha, beta);
                if (score < beta) beta = score;
                if (alpha >= beta) break;  // alpha cut-off
            }
            return beta;
        }
    }


}