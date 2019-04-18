package student_player;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class Agent {


    public static PentagoMove findBestMove(PentagoBoardState pentagoBoardState) {


        // Convert the PentagoBoardState to a bitboard
        PentagoBitBoard boardState = new PentagoBitBoard(pentagoBoardState);

        return PentagoBitMove.bitMoveToPentagoMove(boardState.getRandomMove());
    }
}
