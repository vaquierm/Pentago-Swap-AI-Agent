package pentago_agent;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;

import java.util.List;


public class PentagoAgent extends PentagoPlayer {

    /**
     * Time allocated for the agent to make a decision
     */
    private static final long TIMEOUT = 2000;


    public PentagoAgent() {
        super("Thanos Tier Ultimate Agent");
    }


    /**
     * Chooses a move to play
     * @param pentagoBoardState The current board state
     * @return The moves that the agent plays
     */
    public Move chooseMove(PentagoBoardState pentagoBoardState) {

        long startTime = System.currentTimeMillis();

        // Convert the PentagoBoardState to a bitboard
        PentagoBitBoard boardState = new PentagoBitBoard(pentagoBoardState);

        System.out.println("Player " + pentagoBoardState.getTurnPlayer() + " playing move " + boardState.getTurnNumber());

        List<Long> moves = MoveFilter.getNonDangerousMoves(boardState);

        // Pick the best move to play, allocate the time remaining to do so.
        long moveToPlay = MovePicker.pickMoveFromSet(TIMEOUT - System.currentTimeMillis() + startTime, boardState, moves);

        System.out.println("Move decided in " + (System.currentTimeMillis() - startTime) + " ms, returning move: " + PentagoBitMove.toPrettyString(moveToPlay));

        return PentagoBitMove.bitMoveToPentagoMove(moveToPlay);
    }
}