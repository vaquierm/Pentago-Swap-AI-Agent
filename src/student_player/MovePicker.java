package student_player;

import java.util.List;

public class MovePicker {

    /**
     * Pick the best move to play among non dangerous moves.
     * @param boardState The current board state
     * @param setOfMoves The set of moves to chose from
     * @return The best move to play.
     */
    public static long pickMoveFromSet(PentagoBitBoard boardState, List<Long> setOfMoves) {

        // If there is only one move to pick from return the move.
        if (setOfMoves.size() == 1) {
            return setOfMoves.get(0);
        }

        System.out.println("Computing what move to play out of " + setOfMoves.size() + " least dangerous moves.");

        int player = boardState.getTurnPlayer();

        // Initialize the best move value
        int bestMoveValue = Integer.MIN_VALUE;
        long bestBitMove = 0;

        // Iterate through all moves to determine which one is best
        for (Long move : setOfMoves) {

            // Process the move
            boardState.processMove(move);

            int tempScore = boardState.evaluateBoard(player);

            if (tempScore > bestMoveValue) {
                bestBitMove = move;
                bestMoveValue = tempScore;
            }

            boardState.undoMove(move);

        }

        return bestBitMove;
    }


}
