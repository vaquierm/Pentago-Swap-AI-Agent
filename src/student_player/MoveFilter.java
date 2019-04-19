package student_player;

import java.util.ArrayList;
import java.util.List;

public class MoveFilter {

    private static List<Long> firstLayerElimination;
    private static List<Long> secondLayerElimination;
    private static List<Long> criticalStateMoves;

    public static List<Long> getNonDangerousMoves(PentagoBitBoard boardState) {

        List<Long> moves = boardState.getAllLegalNonSymmetricMoves();

        firstLayerElimination = new ArrayList<>(moves.size());
        secondLayerElimination = new ArrayList<>(moves.size());
        criticalStateMoves = new ArrayList<>(moves.size());

        firstLayerFilter(moves, boardState);

        moves.removeAll(firstLayerElimination);
        if (moves.isEmpty()) {
            System.out.println("No more options, must make opponent win...");
            return firstLayerElimination;
        }

        moves.removeAll(secondLayerElimination);
        if (moves.isEmpty()) {
            System.out.println("No more hope, cornered by opponent...");
            return secondLayerElimination;
        }

        while (!criticalStateMoves.isEmpty()) {
            if (moves.contains(criticalStateMoves.get(criticalStateMoves.size() - 1))) {
                System.out.println("Found safe critical state move");
                moves = new ArrayList<>(1);
                moves.add(criticalStateMoves.get(criticalStateMoves.size() - 1));
                break;
            }
        }

        return moves;
    }


    private static void firstLayerFilter(List<Long> moves, PentagoBitBoard boardState) {

        int player = boardState.getTurnPlayer();

        for (Long move : moves) {

            boardState.processMove(move);

            long[] winMove = boardState.getWinMove(player);

            if (winMove[1] > 1) {
                criticalStateMoves.add(move);
            }

            // This move makes the opponent win. Remove it
            if (boardState.getWinner() == 1 - player) {
                firstLayerElimination.add(move);
            }
            // Check further in the tree
            else {
                secondLayerFilter(player, move, boardState);
            }

            boardState.undoMove(move);
        }

    }

    private static void secondLayerFilter(int player, long rootMove, PentagoBitBoard boardState) {

        List<Long> secondLayerMoves = boardState.getAllLegalNonSymmetricMoves();

        for (Long secondMove : secondLayerMoves) {

            boardState.processMove(secondMove);

            // Opponent can win in one move if we perform root move
            if (boardState.getWinner() == 1 - player) {
                secondLayerElimination.add(rootMove);
            }
            else {
                // Check if the opponent is has put us in a critical state
            }

            boardState.undoMove(secondMove);
        }
    }
}
