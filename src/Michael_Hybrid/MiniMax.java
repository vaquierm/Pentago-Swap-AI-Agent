package Michael_Hybrid;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class MiniMax {

    public static PentagoMove computeMove(CustomPentagoBoardState boardState) {
        PentagoMove bestMove = null;

        boolean isWhite = boardState.getTurnPlayer() == 0;

        int levels;

        if (boardState.getTurnPlayer() == 0) {
            if (boardState.getTurnNumber() <= 10)
                levels = 1;
            else if (boardState.getTurnNumber() <= 14)
                levels = 2;
            else if (boardState.getTurnNumber() <= 17)
                levels = 3;
            else
                levels = 4;
        }
        else
        {
            if (boardState.getTurnNumber() <= 10)
                levels = 1;
            else if (boardState.getTurnNumber() <= 13)
                levels = 2;
            else if (boardState.getTurnNumber() <= 16)
                levels = 3;
            else
                levels = 4;
        }

        int bestVal = (isWhite) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (PentagoMove move : boardState.getAllLegalMovesWithSymmetry()) {

            // Process the move
            boardState.processMove(move);

            // Determine how good the move is
            int val = minimax(boardState, levels, !isWhite, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (isWhite && val > bestVal) {
                bestVal = val;
                bestMove = move;
            } else if (!isWhite && val < bestVal) {
                bestVal = val;
                bestMove = move;
            }

            boardState.revertMove(move);
            // Revert the move
        }

        if (bestMove == null) {
            System.out.println("MiniMax no hope");
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


}
