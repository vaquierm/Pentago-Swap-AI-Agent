package benchmark_player;

import boardgame.Move;

import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("Random Non-Stupid Benchmark");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...

        PentagoMove bestMove = null;
        int bestMoveVal = Integer.MIN_VALUE;
        PentagoBoardState tempBoard;
        for (PentagoMove move : boardState.getAllLegalMoves()) {
            // Clone the board
            tempBoard = (PentagoBoardState) boardState.clone();

            // Process the move
            tempBoard.processMove(move);

            int valueOfNode = MyTools.minimax(boardState.getTurnPlayer(), tempBoard, 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (valueOfNode > bestMoveVal) {
                bestMoveVal = valueOfNode;
                bestMove = move;
            }
        }



        if (bestMove == null) {
            bestMove = (PentagoMove) boardState.getRandomMove();
        }

        return bestMove;
    }
}