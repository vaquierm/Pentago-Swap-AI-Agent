package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class Agent {

    private static final long TIMEOUT = 2000;

    /**
     * Finds the move to play given a particular board state.
     * @param pentagoBoardState Current board state.
     * @return The move to play
     */
    public static PentagoMove findBestMove(PentagoBoardState pentagoBoardState) {

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
