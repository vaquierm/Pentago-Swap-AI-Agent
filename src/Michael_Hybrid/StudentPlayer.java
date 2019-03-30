package Michael_Hybrid;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    private boolean firstMove = true;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("Michael_Hybrid");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        long start = System.currentTimeMillis();
        // Return your move to be processed by the server.
        long timeToPlay = 1500;
        System.out.println("Player " + boardState.getTurnPlayer() + " playing move " + boardState.getTurnNumber());
        Move move = null;
        if (boardState.getTurnNumber() > 11)
            move = Agent.findBestMoveMiniMax(timeToPlay, boardState);

        if (move == null) {

            timeToPlay -= (System.currentTimeMillis() - start);
            move = Agent.findBestMoveMontecarlo(timeToPlay, boardState);

        }

        System.out.println("Move played in " + (System.currentTimeMillis() - start) + "ms");

        return move;
    }
}