package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

import java.util.List;

public class Agent {


    public static PentagoMove findBestMove(PentagoBoardState pentagoBoardState) {

        // Convert the PentagoBoardState to a bitboard
        PentagoBitBoard boardState = new PentagoBitBoard(pentagoBoardState);

        long[] winMove = boardState.getWinMove();

        if (winMove[0] > 0) {
            System.out.println("Win move found: " + PentagoBitMove.toPrettyString(winMove[0]));
            return PentagoBitMove.bitMoveToPentagoMove(winMove[0]);
        }

        List<Long> moves = MoveFilter.getNonDangerousMoves(boardState);

        return PentagoBitMove.bitMoveToPentagoMove(moves.get(((int) (Math.random() * moves.size()))));
    }
}
