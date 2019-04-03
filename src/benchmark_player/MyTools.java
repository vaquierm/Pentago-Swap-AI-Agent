package benchmark_player;

import boardgame.Board;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class MyTools {


    public static int minimax(int myPlayer, PentagoBoardState boardState, int level, boolean max, int alpha, int beta) {
        if (boardState.gameOver() || level == 0) {
            if (boardState.getWinner() == myPlayer)
                return Integer.MAX_VALUE;
            else if (boardState.getWinner() == 1 - myPlayer)
                return Integer.MIN_VALUE;
            else if (boardState.getWinner() == Board.DRAW)
                return (Integer.MAX_VALUE >> 2);

            return (int) (Math.random() * 1000);
        }

        List<PentagoMove> moves = boardState.getAllLegalMoves();
        PentagoBoardState tempBoard;

        int score;

        if (max) {
            // Find max and store in alpha
            for (PentagoMove move : moves) {

                // Clone the board
                tempBoard = (PentagoBoardState) boardState.clone();

                // Play the move
                tempBoard.processMove(move);

                // Recursively check
                score = minimax(myPlayer, tempBoard, level - 1, false, alpha, beta);


                // Update the values
                if (score > alpha) alpha = score;
                if (alpha >= beta) break;  // beta cut-off
            }
            return alpha;
        } else {
            // Find min and store in beta
            for (PentagoMove move : moves) {

                // Clone the board
                tempBoard = (PentagoBoardState) boardState.clone();

                // Play the move
                tempBoard.processMove(move);

                // Recursively check
                score = minimax(myPlayer, tempBoard, level - 1, false, alpha, beta);


                // Update the values
                if (score < beta) beta = score;
                if (alpha >= beta) break;  // alpha cut-off
            }
            return beta;
        }
    }

}