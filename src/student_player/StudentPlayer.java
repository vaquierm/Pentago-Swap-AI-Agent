package student_player;

import boardgame.Move;

import pentago_swap.PentagoPlayer;
import pentago_swap.PentagoBoardState;


public class StudentPlayer extends PentagoPlayer {

    public StudentPlayer() {
        super("Thanos Tier Ultimate Agent");
    }


    /**
     * Chooses a move to play
     * @param boardState The current board state
     * @return The moves that the agent plays
     */
    public Move chooseMove(PentagoBoardState boardState) {

        return Agent.findBestMove(boardState);
    }
}