package student_player;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;
import student_player.UtilTools.Symmetry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.UnaryOperator;
import java.util.Random;

/**
 *
 * Note: First player white, second player black!!
 * @author mgrenander
 */
public class CustomPentagoBoardState extends BoardState {
    public static final int BOARD_SIZE = 6;
    private static final int QUAD_SIZE = 3;
    private static final int NUM_QUADS = 4;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int MAX_TURNS = 18;
    private static final int ILLEGAL = -1;

    private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    private static int FIRST_PLAYER = WHITE;
    private static HashMap<Quadrant, Integer> quadToInt;
    private static HashMap<Integer, Quadrant> intToQuad;
    static {
        quadToInt = new HashMap<>(4);
        quadToInt.put(Quadrant.TL, 0);
        quadToInt.put(Quadrant.TR, 1);
        quadToInt.put(Quadrant.BL, 2);
        quadToInt.put(Quadrant.BR, 3);
        intToQuad = new HashMap<>(4);
        intToQuad.put(0, Quadrant.TL);
        intToQuad.put(1, Quadrant.TR);
        intToQuad.put(2, Quadrant.BL);
        intToQuad.put(3, Quadrant.BR);
    }

    private Piece[][] board;
    private Piece[][][] quadrants;
    private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand;

    public CustomPentagoBoardState() {
        super();
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = Piece.EMPTY;
            }
        }
        this.quadrants = new Piece[NUM_QUADS][QUAD_SIZE][QUAD_SIZE];
        for (int i = 0; i < NUM_QUADS; i++) {
            for (int j = 0; j < QUAD_SIZE; j++) {
                for (int k = 0; k < QUAD_SIZE; k++) {
                    this.quadrants[i][j][k] = Piece.EMPTY;
                }
            }
        }

        rand = new Random(2019);
        winner = Board.NOBODY;
        turnPlayer = FIRST_PLAYER;
        turnNumber = 0;
    }

    // For cloning
    private CustomPentagoBoardState(CustomPentagoBoardState pbs) {
        super();
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(pbs.board[i], 0, this.board[i], 0, BOARD_SIZE);
        }
        this.quadrants = new Piece[NUM_QUADS][QUAD_SIZE][QUAD_SIZE];
        for (int i = 0; i < NUM_QUADS; i++) {
            for (int j = 0; j < QUAD_SIZE; j++) {
                System.arraycopy(pbs.quadrants[i][j], 0, this.quadrants[i][j], 0, QUAD_SIZE);
            }
        }

        rand = new Random(2019);
        this.winner = pbs.winner;
        this.turnPlayer = pbs.turnPlayer;
        this.turnNumber = pbs.turnNumber;
    }

    public CustomPentagoBoardState(PentagoBoardState pbs) {
        super();
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];

        this.quadrants = new Piece[NUM_QUADS][QUAD_SIZE][QUAD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boolean isLeftQuadMove = j / QUAD_SIZE == 0;
                boolean isTopQuadMove = i / QUAD_SIZE == 0;
                if (isLeftQuadMove && isTopQuadMove) { //Top left quadrant
                    quadrants[0][i][j] = pbs.getPieceAt(i, j);
                } else if (!isLeftQuadMove && isTopQuadMove) { //Top right quadrant
                    quadrants[1][i][j % QUAD_SIZE] = pbs.getPieceAt(i, j);
                } else if (isLeftQuadMove) { //Bottom left quadrant
                    quadrants[2][i % QUAD_SIZE][j] = pbs.getPieceAt(i, j);
                } else { //Bottom right quadrant
                    quadrants[3][i % QUAD_SIZE][j % QUAD_SIZE] = pbs.getPieceAt(i, j);
                }
            }
        }

        buildBoardFromQuadrants();

        rand = new Random(2019);
        this.winner = pbs.getWinner();
        this.turnPlayer = pbs.getTurnPlayer();
        this.turnNumber = pbs.getTurnNumber();
    }

    Piece[][] getBoard() { return this.board; }

    @Override
    public Object clone() {
        return new CustomPentagoBoardState(this);
    }

    @Override
    public int getWinner() { return winner; }

    @Override
    public void setWinner(int win) { winner = win; }

    @Override
    public int getTurnPlayer() { return turnPlayer; }

    @Override
    public int getTurnNumber() { return turnNumber; }

    @Override
    public boolean isInitialized() { return board != null; }

    @Override
    public int firstPlayer() { return FIRST_PLAYER; }

    public int getOpponent() { return (turnPlayer == WHITE) ? BLACK : WHITE; }

    @Override
    public Move getRandomMove() {
        ArrayList<CustomPentagoMove> moves = getAllLegalMoves();
        //return moves.get(rand.nextInt(moves.size()));
        return moves.get((int)(Math.random() * moves.size()));
    }

    public Piece getPieceAt(int xPos, int yPos) {
        if (xPos < 0 || xPos >= BOARD_SIZE || yPos < 0 || yPos >= BOARD_SIZE) {
            throw new IllegalArgumentException("Out of range");
        }
        return board[xPos][yPos];
    }

    public Piece getPieceAt(PentagoCoord coord) {
        return getPieceAt(coord.getX(), coord.getY());
    }

    public ArrayList<CustomPentagoMove> getAllLegalMoves() {
        ArrayList<CustomPentagoMove> legalMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    for (int k = 0; k < NUM_QUADS - 1; k++) { // Iterate through valid swaps
                        for (int l = k+1; l < NUM_QUADS; l++) {
                            legalMoves.add(new CustomPentagoMove(i, j, intToQuad.get(k), intToQuad.get(l), turnPlayer));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public HashSet<CustomPentagoMove> getAllLegalMovesAsHashSet() {
        HashSet<CustomPentagoMove> legalMoves = new HashSet<>();
        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    for (int k = 0; k < NUM_QUADS - 1; k++) { // Iterate through valid swaps
                        for (int l = k+1; l < NUM_QUADS; l++) {
                            legalMoves.add(new CustomPentagoMove(i, j, intToQuad.get(k), intToQuad.get(l), turnPlayer));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public ArrayList<CustomPentagoMove> getAllLegalMovesWithSymemry() {
        HashSet<Symmetry> symmetries = UtilTools.checkSymmetry(board);

        //TODO: Based on the symmetry of the board, remove duplicate moves

        for (Symmetry symmetry : symmetries) {

        }

        return null;
    }

    /**
     * Check if the given move is legal
     * @param m the move
     * @return true if the move is legal, false otherwise
     */
    public boolean isLegal(CustomPentagoMove m) {
        if (m.getASwap() == m.getBSwap()) { return false; } // Cannot swap same tile
        PentagoCoord c = m.getMoveCoord();
        if (c.getX() >= BOARD_SIZE || c.getX() < 0 || c.getY() < 0 || c.getY() >= BOARD_SIZE) { return false; }
        if (turnPlayer != m.getPlayerID() || m.getPlayerID() == ILLEGAL) { return false; } //Check right player
        return board[c.getX()][c.getY()] == Piece.EMPTY;
    }

    /**
     * Check if placing a piece here is legal, without regards to the swap or player ID
     * @param c coordinate for the piece
     * @return true if piece can be played here, false otherwise
     */
    public boolean isPlaceLegal(PentagoCoord c) {
        if (c.getX() >= BOARD_SIZE || c.getX() < 0 || c.getY() < 0 || c.getY() >= BOARD_SIZE) { return false; }
        return board[c.getX()][c.getY()] == Piece.EMPTY;
    }

    public void processMove(CustomPentagoMove m) throws IllegalArgumentException {
        if (!isLegal(m)) { throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString()); }
        updateQuadrants(m);
        updateWinner();
        if (turnPlayer != FIRST_PLAYER) { turnNumber += 1; } // Update the turn number if needed
        turnPlayer = 1 - turnPlayer; // Swap player
    }

    /**
     * Updates the appropriate quandrant based on the location of the move m
     * @param m: Pentago move
     */
    private void updateQuadrants(CustomPentagoMove m) {
        Piece turnPiece = turnPlayer == WHITE ? Piece.WHITE : Piece.BLACK;
        int x = m.getMoveCoord().getX();
        int y = m.getMoveCoord().getY();
        boolean isLeftQuadMove = y / 3 == 0;
        boolean isTopQuadMove = x / 3 == 0;
        if (isLeftQuadMove && isTopQuadMove) { //Top left quadrant
            quadrants[0][x][y] = turnPiece;
        } else if (!isLeftQuadMove && isTopQuadMove) { //Top right quadrant
            quadrants[1][x][y % QUAD_SIZE] = turnPiece;
        } else if (isLeftQuadMove) { //Bottom left quadrant
            quadrants[2][x % QUAD_SIZE][y] = turnPiece;
        } else { //Bottom right quadrant
            quadrants[3][x % QUAD_SIZE][y % QUAD_SIZE] = turnPiece;
        }

        //Swapping mechanism
        int a = quadToInt.get(m.getASwap());
        int b = quadToInt.get(m.getBSwap());
        Piece[][] tmp = quadrants[a];
        quadrants[a] = quadrants[b];
        quadrants[b] = tmp;

        buildBoardFromQuadrants();
    }

    /**
     * Updates the board after the quadrants have changed.
     */
    private void buildBoardFromQuadrants() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            int quadrantRow = i < 3 ? i : i - 3;
            int leftQuad = i < 3 ? 0 : 2;
            int rightQuad = i < 3 ? 1 : 3;
            System.arraycopy(quadrants[leftQuad][quadrantRow], 0, board[i], 0, 3);
            System.arraycopy(quadrants[rightQuad][quadrantRow], 0, board[i], 3, 3);
        }
    }

    /**
     * Checks if the game has ended, and changes the winner attribute if so.
     */
    private void updateWinner() {
        boolean playerWin = checkVerticalWin(turnPlayer) || checkHorizontalWin(turnPlayer) || checkDiagRightWin(turnPlayer) || checkDiagLeftWin(turnPlayer);
        int otherPlayer = 1 - turnPlayer;
        boolean otherWin = checkVerticalWin(otherPlayer) || checkHorizontalWin(otherPlayer) || checkDiagRightWin(otherPlayer) || checkDiagLeftWin(otherPlayer);
        if (playerWin) { // Current player has won
            winner = otherWin ? Board.DRAW : turnPlayer;
        } else if (otherWin) { // Player's move caused the opponent to win
            winner = otherPlayer;
        } else if (gameOver()) {
            winner = Board.DRAW;
        }
    }

    @Override
    public boolean gameOver() {
        return ((turnNumber >= MAX_TURNS - 1) && turnPlayer == BLACK) || winner != Board.NOBODY;
    }

    private boolean checkVerticalWin(int player) {
        return checkWinRange(player, 0, 2, 0, BOARD_SIZE, getNextVertical);
    }

    private boolean checkHorizontalWin(int player) {
        return checkWinRange(player, 0, BOARD_SIZE, 0, 2, getNextHorizontal);
    }

    private boolean checkDiagRightWin(int player) {
        return checkWinRange(player, 0, 2, 0, 2, getNextDiagRight);
    }

    private boolean checkDiagLeftWin(int player) {
        return checkWinRange(player, 0 ,2, BOARD_SIZE - 2, BOARD_SIZE, getNextDiagLeft);
    }

    private boolean checkWinRange(int player, int xStart, int xEnd, int yStart, int yEnd, UnaryOperator<PentagoCoord> direction) {
        boolean win = false;
        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                win |= checkWin(player, new PentagoCoord(i, j), direction);
                if (win) { return true; }
            }
        }
        return false;
    }

    private boolean checkWin(int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction) {
        int winCounter = 0;
        Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
        PentagoCoord current = start;
        while(true) {
            try {
                if (currColour == this.board[current.getX()][current.getY()]) {
                    winCounter++;
                    current = direction.apply(current);
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) { //We have run off the board
                break;
            }
        }
        return winCounter >= 5;
    }

    public void printBoard() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        String rowMarker = "--------------------------\n";
        boardString.append(rowMarker);
        for (int i = 0; i < BOARD_SIZE; i++) {
            boardString.append("|");
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardString.append(" ");
                boardString.append(board[i][j].toString());
                boardString.append(" |");
                if (j == QUAD_SIZE - 1) {
                    boardString.append("|");
                }
            }
            boardString.append("\n");
            if (i == QUAD_SIZE - 1) {
                boardString.append(rowMarker);
            }
        }
        boardString.append(rowMarker);
        return boardString.toString();
    }


    /**
     * Reverts a move made by the board.
     * Note: It is assumed that the move passed was actually made.
     * @param move  The last move that was played that needs to be reverted
     */
    public void revertMove(CustomPentagoMove move) {

        //Swapping mechanism
        int a = quadToInt.get(move.getASwap());
        int b = quadToInt.get(move.getBSwap());
        Piece[][] tmp = quadrants[a];
        quadrants[a] = quadrants[b];
        quadrants[b] = tmp;

        // Remove the piece placed
        int x = move.getMoveCoord().getX();
        int y = move.getMoveCoord().getY();
        boolean isLeftQuadMove = y / QUAD_SIZE == 0;
        boolean isTopQuadMove = x / QUAD_SIZE == 0;
        if (isLeftQuadMove && isTopQuadMove) { //Top left quadrant
            quadrants[0][x][y] = PentagoBoardState.Piece.EMPTY;
        } else if (!isLeftQuadMove && isTopQuadMove) { //Top right quadrant
            quadrants[1][x][y % QUAD_SIZE] = PentagoBoardState.Piece.EMPTY;
        } else if (isLeftQuadMove) { //Bottom left quadrant
            quadrants[2][x % QUAD_SIZE][y] = PentagoBoardState.Piece.EMPTY;
        } else { //Bottom right quadrant
            quadrants[3][x % QUAD_SIZE][y % QUAD_SIZE] = PentagoBoardState.Piece.EMPTY;
        }

        buildBoardFromQuadrants();

        // Reset the player to the previous player and the turn to the previous turn
        if (turnPlayer == FIRST_PLAYER) { turnNumber -= 1; } // Update the turn number if needed
        turnPlayer = 1 - turnPlayer; // Swap player

        updateWinner();
    }

    public boolean boardEquals(PentagoBoardState pbs) {

        if (winner != pbs.getWinner() || turnNumber != pbs.getTurnNumber() || turnPlayer != pbs.getTurnPlayer())
            return false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != pbs.getPieceAt(i, j)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean boardEquals(CustomPentagoBoardState pbs) {

        if (winner != pbs.getWinner() || turnNumber != pbs.getTurnNumber() || turnPlayer != pbs.getTurnPlayer())
            return false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != pbs.getPieceAt(i, j)) {
                    return false;
                }
            }
        }

        return true;
    }




}
