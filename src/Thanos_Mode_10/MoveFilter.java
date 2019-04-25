package Thanos_Mode_10;

import java.util.*;

import static Thanos_Mode_10.PentagoBitBoard.NOBODY;

public class MoveFilter {

    private static List<Long> firstLayerElimination;
    private static List<Long> secondLayerElimination;
    private static List<Long> criticalStateMoves;
    private static List<Long> criticalStateSecondMove;
    private static List<Long> opponentCriticalStateMove;

    public static List<Long> getNonDangerousMoves(PentagoBitBoard boardState) {

        // Check to see if the agent can win from this board state
        long winMove = boardState.getWinMove(boardState.getTurnPlayer());

        if (winMove > 0) {
            System.out.println("Win move found: " + PentagoBitMove.toPrettyString(winMove));
            List<Long> winMoveList = new ArrayList<>(1);
            winMoveList.add(winMove);
            return winMoveList;
        }

        System.out.println("Filtering bad moves.");

        List<Long> moves = boardState.getAllLegalNonSymmetricMoves();

        System.out.println("The agent has the possibility to play " + moves.size() + " moves");

        firstLayerElimination = new ArrayList<>(moves.size());
        secondLayerElimination = new ArrayList<>(moves.size());
        criticalStateMoves = new ArrayList<>(moves.size());
        criticalStateSecondMove = new ArrayList<>(moves.size());
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


        if (!criticalStateSecondMove.isEmpty()) {
            System.out.println("There is a total of " + criticalStateSecondMove.size() + " potential two move away critical moves to play");
        }
        while (!criticalStateSecondMove.isEmpty()) {
            if (moves.contains(criticalStateSecondMove.get(criticalStateSecondMove.size() - 1))) {
                System.out.println("Found safe two move away critical state move");
                moves = new ArrayList<>(1);
                moves.add(criticalStateSecondMove.get(criticalStateSecondMove.size() - 1));
                return moves;
            }
            else {
                criticalStateSecondMove.remove(criticalStateSecondMove.get(criticalStateSecondMove.size() - 1));
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

            // This move makes the opponent win. Remove it
            if (boardState.getWinner() == 1 - player) {
                firstLayerElimination.add(move);
            }

            // Check if we have put the opponent in a critical state
            if (boardState.isCriticalState()) {
                criticalStateMoves.add(move);
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

        // If the opponent can win from this boardstate after our move, do not consider the move.
        if (!secondLayerElimination.contains(rootMove) && boardState.getWinMove(1 - player) > 0) {
            secondLayerElimination.add(rootMove);
            return;
        }

        boolean twoLayerCritical = true;

        for (Long secondMove : secondLayerMoves) {

            // Play the opponent move.
            boardState.processMove(secondMove);

            // If the opponent player makes the agent win, continue
            if (boardState.getWinner() == NOBODY) {

                // Check if the opponent has put us in a critical state
                if (!opponentCriticalStateMove.contains(rootMove) && boardState.isCriticalState()) {
                    opponentCriticalStateMove.add(rootMove);
                    boardState.undoMove(secondMove);
                    return;
                }
                // If the opponent cannot put us in a critical state, check if we can now put the opponent in a critical state from their move.
                else {

                    if (!twoLayerCritical || !canWinGuaranteed(player, boardState)) {
                        twoLayerCritical = false;
                    }
                }
            }

            boardState.undoMove(secondMove);
        }

        // We can guarantee a win from any of the moves the opponent plays at this level.
        if (twoLayerCritical) {
            criticalStateSecondMove.add(rootMove);
        }
    }


    private static boolean canWinGuaranteed(int player, PentagoBitBoard boardState) {

        // If the player can win return 1;
        if (boardState.getWinner() == player || boardState.getWinMove(player) > 0) {
            return true;
        }

        List<Long> thirdLayerMoves = boardState.getAllLegalNonSymmetricMoves();

        for (Long thirdMove : thirdLayerMoves) {

            boardState.processMove(thirdMove);

            if (boardState.isCriticalState()) {
                boardState.undoMove(thirdMove);
                return true;
            }

            boardState.undoMove(thirdMove);

        }

        return false;
    }

}
