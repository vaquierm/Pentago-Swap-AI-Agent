package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class Agent {


    public static PentagoMove findBestMove(PentagoBoardState pentagoBoardState) {

        System.out.println("Player " + pentagoBoardState.getTurnPlayer() + " playing.");

        // Convert the PentagoBoardState to a bitboard
        PentagoBitBoard boardState = new PentagoBitBoard(pentagoBoardState);

        long[] winMove = boardState.getWinMove(boardState.getTurnPlayer());

        if (winMove[0] > 0) {
            System.out.println("Win move found: " + PentagoBitMove.toPrettyString(winMove[0]));
            return PentagoBitMove.bitMoveToPentagoMove(winMove[0]);
        }

        List<Long> moves = MoveFilter.getNonDangerousMoves(boardState);

        System.out.println("Move decided, returning move");

        return PentagoBitMove.bitMoveToPentagoMove(moves.get(((int) (Math.random() * moves.size()))));
    }
}
