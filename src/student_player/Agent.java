package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class Agent {


    /**
     * Finds the move to play given a particular board state.
     * @param pentagoBoardState Current board state.
     * @return The move to play
     */
    public static PentagoMove findBestMove(PentagoBoardState pentagoBoardState) {

        // Convert the PentagoBoardState to a bitboard
        PentagoBitBoard boardState = new PentagoBitBoard(pentagoBoardState);

        System.out.println("Player " + pentagoBoardState.getTurnPlayer() + " playing move " + boardState.getTurnNumber());

        long winMove = boardState.getWinMove(boardState.getTurnPlayer());

        if (winMove > 0) {
            System.out.println("Win move found: " + PentagoBitMove.toPrettyString(winMove));
            return PentagoBitMove.bitMoveToPentagoMove(winMove);
        }

        List<Long> moves = MoveFilter.getNonDangerousMoves(boardState);

        System.out.println("Move decided, returning move");

        return PentagoBitMove.bitMoveToPentagoMove(moves.get(((int) (Math.random() * moves.size()))));
    }
}
