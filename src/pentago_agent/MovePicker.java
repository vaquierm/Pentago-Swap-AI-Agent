package pentago_agent;

import pentago_agent.Montecarlo.MCTS;

import java.util.List;

public class MovePicker {

    /**
     * Pick the best move to play among non dangerous moves.
     * @param timeout The time allocated to pick a move
     * @param boardState The current board state
     * @param setOfMoves The set of moves to chose from
     * @return The best move to play.
     */
    public static long pickMoveFromSet(long timeout, PentagoBitBoard boardState, List<Long> setOfMoves) {

        // If there is only one move to pick from return the move.
        if (setOfMoves.size() == 1) {
            return setOfMoves.get(0);
        }

        System.out.println("Computing what move to play out of " + setOfMoves.size() + " least dangerous moves.");

        return MCTS.getMCTSBestMove(timeout, setOfMoves, boardState);
    }


}
