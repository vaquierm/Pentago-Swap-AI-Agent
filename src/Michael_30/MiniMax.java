package Michael_30;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class MiniMax {

    public static PentagoMove computeMove(CustomPentagoBoardState boardState) {
        PentagoMove bestMove = null;

        boolean maxPlayer = boardState.getTurnPlayer() == 1;

        int bestVal = Integer.MIN_VALUE;
        for (PentagoMove move : boardState.getAllLegalMovesWithSymmetry()) {
            int val = minimax(boardState, 2, maxPlayer, -100000, 100000);
            if (val > bestVal) {
                bestVal = val;
                bestMove = move;
            }
        }

        return bestMove;
    }


    public static int minimax(CustomPentagoBoardState boardState, int level, boolean max, int alpha, int beta) {
        if (boardState.gameOver() || level == 0) {
            return boardState.evaluate(PentagoBoardState.Piece.WHITE);
        }

        List<PentagoMove> moves = boardState.getAllLegalMovesWithSymmetry();

        int score;

        if (max) {
            // Find max and store in alpha
            for (PentagoMove move : moves) {

                // Play the move
                boardState.processMove(move);

                // Recursively check
                score = minimax(boardState, level - 1, false, alpha, beta);

                // Revert the move

                boardState.revertMove(move);

                // Update the values
                if (score > alpha) alpha = score;
                if (alpha >= beta) break;  // beta cut-off
            }
            return alpha;
        } else {
            // Find min and store in beta
            for (PentagoMove move : moves) {

                // Play the move
                boardState.processMove(move);

                // Recursively check
                score = minimax(boardState, level - 1, false, alpha, beta);

                // Revert the move

                boardState.revertMove(move);

                // Update the values
                if (score < beta) beta = score;
                if (alpha >= beta) break;  // alpha cut-off
            }
            return beta;
        }
    }

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
