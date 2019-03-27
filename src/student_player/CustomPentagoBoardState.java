package student_player;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;
import student_player.UtilTools.Symmetry;

import java.util.*;
import java.util.function.UnaryOperator;

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
    private static HashMap<Integer, PentagoMove> intToMove;

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

        intToMove = new HashMap<>(BOARD_SIZE * BOARD_SIZE * 6 * 2);

        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < NUM_QUADS - 1; k++) { // Iterate through valid swaps
                    for (int l = k + 1; l < NUM_QUADS; l++) {
                        intToMove.put(moveToInt(i, j, intToQuad.get(k), intToQuad.get(l), 0), new PentagoMove(i, j, intToQuad.get(k), intToQuad.get(l), 0));
                        intToMove.put(moveToInt(i, j, intToQuad.get(k), intToQuad.get(l), 1), new PentagoMove(i, j, intToQuad.get(k), intToQuad.get(l), 1));
                    }
                }
            }
        }
    }

    private static int moveToInt(int x, int y, Quadrant A, Quadrant B, int player) {
        int intRepresentation = x;
        intRepresentation = intRepresentation << 3;
        intRepresentation += y;
        intRepresentation = intRepresentation << 2;
        intRepresentation += quadToInt.get(A);
        intRepresentation = intRepresentation << 2;
        intRepresentation += quadToInt.get(B);
        intRepresentation = intRepresentation << 1;
        intRepresentation += player;
        return intRepresentation;
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
        int numLegalMoves = 0;
        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    // There are 6 possible swaps
                    numLegalMoves += 6;
                }
            }
        }

        int randMoveIndex = (int) (Math.random() * numLegalMoves);

        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    if (randMoveIndex < 6) {
                        int swapCount = 0;
                        for (int k = 0; k < NUM_QUADS - 1; k++) { // Iterate through valid swaps
                            for (int l = k+1; l < NUM_QUADS; l++) {
                                if (swapCount == randMoveIndex) {
                                    return intToMove.get(moveToInt(i, j, intToQuad.get(k), intToQuad.get(l), turnPlayer));
                                }
                                swapCount++;
                            }
                        }
                    }
                    randMoveIndex -= 6;
                }
            }
        }
        return null;
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

    public ArrayList<PentagoMove> getAllLegalMoves() {
        ArrayList<PentagoMove> legalMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) { //Iterate through positions on board
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == Piece.EMPTY) {
                    for (int k = 0; k < NUM_QUADS - 1; k++) { // Iterate through valid swaps
                        for (int l = k+1; l < NUM_QUADS; l++) {
                            legalMoves.add(intToMove.get(moveToInt(i, j, intToQuad.get(k), intToQuad.get(l), turnPlayer)));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    public ArrayList<PentagoMove> getAllLegalMovesWithSymmetry() {
        HashSet<Symmetry> symmetries = UtilTools.checkSymmetry(board);

        ArrayList<PentagoMove> legalMoves = getAllLegalMoves();
        ArrayList<PentagoMove> moves = new ArrayList<>();

        int halfBoard = BOARD_SIZE / 2;

        for (PentagoMove move : legalMoves) {
            PentagoCoord coord = move.getMoveCoord();
            if (symmetries.contains(Symmetry.VERTICAL) && coord.getY() >= halfBoard) {
                continue;
            }
            else if (symmetries.contains(Symmetry.HORIZONTAL) && coord.getX() >= halfBoard) {
                continue;
            }
            else if (symmetries.contains(Symmetry.DIAGONAL1) && coord.getX() > coord.getY()) {
                continue;
            }
            else if (symmetries.contains(Symmetry.DIAGONAL2) && coord.getX() + coord.getY() > BOARD_SIZE - 1) {
                continue;
            }
            moves.add(move);
        }

        return moves;
    }

    /**
     * @return  The list of valid moves taking into account symmetry, and around the opponent piece.
     * Note: Assumes that there is only one opponent piece on the board.
     */
    public List<PentagoMove> getAllLegalMovesWithSymmetryAroundOpponent() {

        List<PentagoMove> moves = getAllLegalMovesWithSymmetry();
        List<PentagoMove> movesAroundOpponent = new LinkedList<>();

        Piece opponent = (turnPlayer == BLACK) ? Piece.WHITE : Piece.BLACK;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == opponent) {
                    for (PentagoMove move : moves) {
                        if (!movesAroundOpponent.contains(move) && move.getMoveCoord().getX() / QUAD_SIZE == i / QUAD_SIZE && move.getMoveCoord().getY() / QUAD_SIZE == j / QUAD_SIZE && move.getMoveCoord().getX() + move.getMoveCoord().getY() - i - j < 3) {
                            movesAroundOpponent.add(move);
                        }
                    }
                }
            }
        }

        return movesAroundOpponent;
    }

    /**
     * Check if the given move is legal
     * @param m the move
     * @return true if the move is legal, false otherwise
     */
    public boolean isLegal(PentagoMove m) {
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

    public void processMove(PentagoMove m) throws IllegalArgumentException {
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
    private void updateQuadrants(PentagoMove m) {
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
        winner = Board.NOBODY;
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
    public void revertMove(PentagoMove move) {

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

    /**
     * Check if the two boards are equal
     * @param pbs  Other board
     * @return True if the two boards are equal
     */
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


    /**
     * Check if the two boards are equal
     * @param pbs  Other board
     * @return True if the two boards are equal
     */
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

    /**
     * Evaluate a heuristic on how good the current board state is
     * @return  The value of the board state
     */
    public int evaluate(Piece piece) {
        if (gameOver()) {
            if (winner == 0) {
                return Integer.MAX_VALUE;
            }
            else if (winner == 1) {
                return  Integer.MAX_VALUE;
            }
            else return 0;
        }

        int val = computePatternValuesForPiece(Piece.WHITE, (piece == Piece.WHITE)) - computePatternValuesForPiece(Piece.BLACK, piece == Piece.WHITE);

        return (piece == Piece.WHITE) ? val : - val;
    }

    private static int[] bitMasksForPairs = {      0b100100000, 0b010010000, 0b001001000, 0b000100100, 0b000010010, 0b000001001, // Pairs of verticals
            0b110000000, 0b011000000, 0b000110000, 0b000011000, 0b000000110, 0b000000011, // Pairs of horizontals
            0b100010000, 0b010001000, 0b000100010, 0b000010001, // Pairs of diagonal1
            0b010100000, 0b001010000, 0b000010100, 0b000001010}; // Pairs of diagonal2

    private static int[] bitMasksForAntiPairs = {  0b000000100, 0b000000010, 0b000000001, 0b100000000, 0b010000000, 0b001000000,
            0b001000000, 0b100000000, 0b000001000, 0b000100000, 0b000000001, 0b000000100,
            0b000000001, 0b000000000, 0b000000000, 0b100000000,
            0b000000000, 0b000000100, 0b001000000, 0b000000000};

    private static int[] twoEndsBitMasks = {       0b101000000, 0b000101000, 0b000000101, 0b100000100, 0b010000101, 0b001000001, 0b100000001, 0b001000100 };

    private static int[] twoEndsBlockBitMasks = {  0b010000000, 0b000010000, 0b000000010, 0b000100000, 0b000010000, 0b000001000, 0b000010000, 0b000010000 };

    private static int[][] patternPresent = new int[4][bitMasksForPairs.length];
    private static int[][] patternPresentOpponent = new int[4][bitMasksForPairs.length];
    private static int[][] antiPatternPresent = new int[4][bitMasksForAntiPairs.length];
    private static int[][] antiPatternPresentOpponent = new int[4][bitMasksForAntiPairs.length];

    /**
     * Compute the sum of the pattern values of each quadrant for a piece
     * @param piece  The piece to look for
     * @param offensiveMode  Evaluate the board differently based on this parameter. if not offensive mode, value blocks a lot more
     * @return  The pattern value
     */
    private int computePatternValuesForPiece(Piece piece, boolean offensiveMode) {

        int overallScore = 0;

        int pairScore = (offensiveMode) ? 2 : 1;
        int blockScore = (offensiveMode) ? 1 : 4;

        int[] quadrantValues = getQuadrantIntValue(piece);
        int[] quadrantValuesOpponent = getQuadrantIntValue((piece == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);

        for (int i = 0; i < bitMasksForPairs.length; i++) {
            for (int k = 0; k < 4; k++) {
                int temp = bitMasksForPairs[i] & quadrantValues[k]; // Check if the pattern is present in the quadrant
                if (temp == bitMasksForPairs[i]) {
                    // If the pattern is present in the quadrant, indicate that in the flags array
                    patternPresent[k][i]++;
                    // Update the overall score that a pattern was found
                    overallScore+=pairScore;
                }
                temp = bitMasksForPairs[i] & quadrantValuesOpponent[k];
                if (temp == bitMasksForPairs[i]) {
                    // If the pattern is present in the quadrant for the opponent, indicate that in the flags array
                    patternPresentOpponent[k][i]++;
                    // Update the overall score that a pattern was found for the opponent
                    overallScore-=pairScore;
                }
                temp = bitMasksForAntiPairs[i] & quadrantValues[k]; //Check if the anti pattern is present in the quadrant
                if (patternPresentOpponent[k][i] > 0 && temp == bitMasksForAntiPairs[i]) {
                    // If the anti pattern is present in the quadrant, and it is blocking the opponent's pattern, indicate that in the flags array
                    antiPatternPresent[k][i]++;
                    // Update the overall score that a pattern was found for the opponent
                    overallScore+=blockScore;
                }
                temp = bitMasksForAntiPairs[i] & quadrantValuesOpponent[k]; //Check if the anti pattern is present in the quadrant for the opponent
                if (patternPresent[k][i] > 0 && temp == bitMasksForAntiPairs[i]) {
                    // If the anti pattern is present in the quadrant, and it is blocking the opponent's pattern, indicate that in the flags array
                    antiPatternPresentOpponent[k][i]++;
                    // Update the overall score that the pattern was found for the opponent blocking your pattern
                    overallScore-=blockScore;
                }
            }
        }

        // Check if there is any major blocks
        for (int i = 0; i < twoEndsBitMasks.length; i++) {
            for (int k = 0; k < 4; k++) {
                // if the two pieces are in the corner and it is blocked that is worth points
                if ((quadrantValues[k] & twoEndsBitMasks[i]) == twoEndsBitMasks[i] && (quadrantValuesOpponent[k] & twoEndsBlockBitMasks[i]) == twoEndsBlockBitMasks[i]) {
                    overallScore -= (offensiveMode) ? 3 : 6;
                }
                if ((quadrantValuesOpponent[k] & twoEndsBitMasks[i]) == twoEndsBitMasks[i] && (quadrantValues[k] & twoEndsBlockBitMasks[i]) == twoEndsBlockBitMasks[i]) {
                    overallScore += (offensiveMode) ? 3 : 6;
                }
            }
        }

        // Now that the patterns has been found, add bonus

        // Horizontal pieces
        for (int i = 6; i < 12; i+=2){
            overallScore += computeBonusOccurrences(patternPresent, i, i+1);
        }

        // Vertical pieces
        for (int i = 0; i < 3; i++){
            overallScore += computeBonusOccurrences(patternPresent, i, i+3);
        }

        // Diagonal
        overallScore += computeBonusOccurrences(patternPresent, 12, 15);
        overallScore += computeBonusOccurrences(patternPresent, 17, 18);

        for (int i = 0; i < bitMasksForPairs.length; i++) {
            overallScore += computeBonusSamePatternInDiffQuad(patternPresent, i);
        }


        return overallScore;

    }

    /**
     * Check if the pattern exists in more than one quadrant
     * @param patternPresent  The pattern array
     * @param index  Index of pattern to check for
     * @return  The bonus rewarded
     */
    private int computeBonusSamePatternInDiffQuad(int[][] patternPresent, int index) {

        int occurences = 0;
        for (int i = 0; i < 4; i++) {
            if (patternPresent[i][index] == 1) {
                occurences++;
            }
        }

        return (occurences > 1) ? 2 : 0;
    }

    /**
     * Reward for having a combination of these two patterns in any 2 quadrants
     * @param patternPresent  The pattern present array
     * @param index1  The first pattern
     * @param index2  The second pattern
     * @return  Bonus rewarded
     */
    private int computeBonusOccurrences(int[][] patternPresent, int index1, int index2) {
        int bonus = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (patternPresent[i][index1] > 0 && patternPresent[j][index2] > 0) {
                    bonus+=2;
                    break;
                }
            }
        }

        return bonus;
    }



    /**
     * Gets the value of the quadrant as an integer with respect to a piece
     * w w
     *   w w
     *   w
     * would return
     * 110011010
     * @param piece  The piece to check for
     * @return  The integer representation of the quadrant
     */
    private int[] getQuadrantIntValue(Piece piece) {

        int[] quadValues = new int[4];
        for (int i = 0; i < QUAD_SIZE; i++) {
            for (int j = 0; j < QUAD_SIZE; j++) {
                for (int k = 0; k < 3; k++) {
                    // If the piece exists at this position, add
                    if (quadrants[k][i][j] == piece) {
                        quadValues[k]++;
                    }
                    // shift by a bit
                    quadValues[k] = quadValues[k] << 1;
                }
            }
        }

        // shift back 1 bit and return
        quadValues[0] = quadValues[0] >> 1;
        quadValues[1] = quadValues[1] >> 1;
        quadValues[2] = quadValues[2] >> 1;
        quadValues[3] = quadValues[3] >> 1;

        return quadValues;
    }

    /**
     * @return  True if only one or three moves has been played so far, False otherwise
     */
    public boolean boardOneOrThreeMoves() {
        int pieceCount = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != Piece.EMPTY) {
                    pieceCount++;
                }
                if (pieceCount > 5) {
                    return false;
                }
            }
        }

        return pieceCount == 1 || pieceCount == 3 || pieceCount == 5;
    }


}
