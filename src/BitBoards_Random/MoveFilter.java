package BitBoards_Random;

import java.util.*;

public class MoveFilter {

    private static List<Long> firstLayerElimination;
    private static List<Long> secondLayerElimination;
    private static List<Long> criticalStateMoves;
    private static List<Long> potentialWinMoves;
    private static List<Long> opponentCriticalStateMove;

    public static List<Long> getNonDangerousMoves(PentagoBitBoard boardState) {

        List<Long> moves = boardState.getAllLegalNonSymmetricMoves();

        firstLayerElimination = new ArrayList<>(moves.size());
        secondLayerElimination = new ArrayList<>(moves.size());
        criticalStateMoves = new ArrayList<>(moves.size());
        potentialWinMoves = new ArrayList<>(moves.size());
        opponentCriticalStateMove = new ArrayList<>(moves.size());

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
            else {
                criticalStateMoves.remove(criticalStateMoves.get(criticalStateMoves.size() - 1));
            }
        }

        moves.removeAll(opponentCriticalStateMove);
        if (moves.isEmpty()) {
            System.out.println("The opponent could put the agent in a critical state...");
            return opponentCriticalStateMove;
        }

        moves.sort(Comparator.comparingInt(PentagoBitMove::getPriority));

        return moves;
    }


    private static void firstLayerFilter(List<Long> moves, PentagoBitBoard boardState) {

        int player = boardState.getTurnPlayer();

        for (Long move : moves) {

            boardState.processMove(move);

            if (boardState.isCriticalState()) {
                criticalStateMoves.add(move);
            }

            long winMove = boardState.getWinMove(player);

            if (winMove > 1) {
                potentialWinMoves.add(PentagoBitMove.setPriority(move, PentagoBitMove.getPriority(winMove)));
            }

            // This move makes the opponent win. Remove it
            if (boardState.getWinner() == 1 - player) {
                firstLayerElimination.add(PentagoBitMove.setPriority(move, PentagoBitMove.getPriority(winMove)));
            }
            // Check further in the tree
            else {
                secondLayerFilter(player, PentagoBitMove.setPriority(move, PentagoBitMove.getPriority(winMove)), boardState);
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
                if (boardState.isCriticalState()) {
                    opponentCriticalStateMove.add(rootMove);
                }
            }

            boardState.undoMove(secondMove);
        }
    }
}
