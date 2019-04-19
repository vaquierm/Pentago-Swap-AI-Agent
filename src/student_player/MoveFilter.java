package student_player;

import java.util.*;

public class MoveFilter {

    private static List<Long> firstLayerElimination;
    private static List<Long> secondLayerElimination;
    private static List<Long> criticalStateMoves;
    private static List<Long> opponentCriticalStateMove;

    public static List<Long> getNonDangerousMoves(PentagoBitBoard boardState) {

        List<Long> moves = boardState.getAllLegalNonSymmetricMoves();

        System.out.println("The agent has the possibility to play " + moves.size() + " moves");

        firstLayerElimination = new ArrayList<>(moves.size());
        secondLayerElimination = new ArrayList<>(moves.size());
        criticalStateMoves = new ArrayList<>(moves.size());
        opponentCriticalStateMove = new ArrayList<>(moves.size());

        firstLayerFilter(moves, boardState);

        moves.removeAll(firstLayerElimination);
        if (moves.isEmpty()) {
            System.out.println("No more options, must make opponent win...");
            return firstLayerElimination;
        }
        else {
            System.out.println("Removed " + firstLayerElimination.size() + " moves which leads to the opponent winning");
        }

        moves.removeAll(secondLayerElimination);
        if (moves.isEmpty()) {
            System.out.println("No more hope, cornered by opponent...");
            secondLayerElimination.removeAll(firstLayerElimination);
            return secondLayerElimination;
        }
        else {
            System.out.println("Removed " + secondLayerElimination.size() + " moves which leads to the opponent winning in one move");
        }

        if (!criticalStateMoves.isEmpty()) {
            System.out.println("There is a total of " + criticalStateMoves.size() + " potential critical moves to play");
        }
        while (!criticalStateMoves.isEmpty()) {
            if (moves.contains(criticalStateMoves.get(criticalStateMoves.size() - 1))) {
                System.out.println("Found safe critical state move");
                moves = new ArrayList<>(1);
                moves.add(criticalStateMoves.get(criticalStateMoves.size() - 1));
                return moves;
            }
            else {
                criticalStateMoves.remove(criticalStateMoves.get(criticalStateMoves.size() - 1));
            }
        }

        moves.removeAll(opponentCriticalStateMove);
        if (moves.isEmpty()) {
            System.out.println("The opponent could put the agent in a critical state...");
            opponentCriticalStateMove.removeAll(firstLayerElimination);
            opponentCriticalStateMove.removeAll(secondLayerElimination);
            return opponentCriticalStateMove;
        }
        else {
            System.out.println("Removed " + opponentCriticalStateMove.size() + " moves where the opponent could put the agent in a critical state");
        }

        System.out.println("After filtering, the opponent has " + moves.size() + " possible moves to play");

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

        boolean critical = false;

        for (Long secondMove : secondLayerMoves) {

            boardState.processMove(secondMove);

            // Opponent can win in one move if we perform root move
            if (!secondLayerElimination.contains(rootMove) && boardState.getWinner() == 1 - player) {
                secondLayerElimination.add(rootMove);
                boardState.undoMove(secondMove);
                break;
            }
            else {
                // Check if the opponent is has put us in a critical state
                if (!critical && !opponentCriticalStateMove.contains(rootMove) && boardState.isCriticalState()) {
                    opponentCriticalStateMove.add(rootMove);
                    critical = true;
                }
            }

            boardState.undoMove(secondMove);
        }
    }

}
