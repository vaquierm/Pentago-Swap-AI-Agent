package Michael_31;

import pentago_swap.PentagoMove;

import java.util.*;

/**
 * This class is used for the tree policy of the monte carlo tree search
 */
public class MonteCarloTreeNode {

    private MonteCarloTreeNode parent;
    private List<MonteCarloTreeNode> children;

    private PentagoMove move;

    private double winRatio = 0;
    private boolean winRatioUpToDate = true;
    private int visitCount = 0;
    private int winCount = 0;

    private int boardHeuristic;

    private Status status;

    public enum Status {
        WON, LOSS, TIE, PROGRESS
    }

    public MonteCarloTreeNode() {
        parent = null;
        children = new LinkedList<>();
        move = null;
        status = Status.PROGRESS;
        boardHeuristic = 0;
    }

    public MonteCarloTreeNode(MonteCarloTreeNode parent, PentagoMove move) {
        this.parent = parent;
        this.move = move;
        children = new LinkedList<>();
        parent.addChild(this);
        status = Status.PROGRESS;
        boardHeuristic = 0;
    }

    public MonteCarloTreeNode(MonteCarloTreeNode parent, PentagoMove move, int boardHeuristic) {
        this.parent = parent;
        this.move = move;
        children = new LinkedList<>();
        parent.addChild(this);
        status = Status.PROGRESS;
        this.boardHeuristic = boardHeuristic;
    }

    public double getWinRatioBoardHeuristicComboScore(boolean offensiveMode) {
        double divTerm = offensiveMode ? 20 : 8;

        double boardVal = (boardHeuristic < 0) ? -Math.log(Math.abs(boardHeuristic) + 1) : Math.log(Math.abs(boardHeuristic) + 1);
        boardVal /= divTerm;

        System.out.println("Win ratio: " + getWinRatio() + ", Board value: " + boardVal);
        return getWinRatio() + boardVal; // TODO: Figure out this
    }

    public double getWinRatio() {
        if (!winRatioUpToDate) {
            if (status == Status.WON) {
                winRatio = 100000;
            }
            else if (status == Status.LOSS) {
                winRatio = -100000;
            }
            else {
                winRatio = ((double) winCount) / ((double) visitCount);
            }
            winRatioUpToDate = true;
        }
        return winRatio;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public PentagoMove getMove() {
        return move;
    }

    public void visit() {
        winRatioUpToDate = false;
        visitCount++;
    }

    public void incrementWinCount(int value) {
        winRatioUpToDate = false;
        winCount += value;
    }

    public void removeParent() {
        parent = null;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void addChild(MonteCarloTreeNode child) {
        children.add(child);
    }

    public void setChildren(List<MonteCarloTreeNode> children) {
        this.children = children;
    }

    public void setParent(MonteCarloTreeNode parent) {
        this.parent = parent;
    }

    public MonteCarloTreeNode getParent() {
        return parent;
    }

    public List<MonteCarloTreeNode> getChildren() {
        return children;
    }

    public double getUCTValue() {
        if (parent == null || status != Status.PROGRESS) {
            return -100000;
        }
        if (visitCount == 0) {
            return 100000;
        }
        if (parent.getVisitCount() == 0)
            return this.getWinRatio();

        return this.getWinRatio() + 1.41 * Math.sqrt(Math.log(parent.getVisitCount()) / (double) visitCount);
    }

    public MonteCarloTreeNode getChildWithBestUCT() {
        return Collections.max(children, Comparator.comparing(MonteCarloTreeNode::getUCTValue));
    }

    public void updateStatus(Status status) {
        this.status = status;
        winRatioUpToDate = false;
    }

    public List<MonteCarloTreeNode> getUnexploredChildren() {
        List<MonteCarloTreeNode> unexploredChildren = new ArrayList<>();

        for (MonteCarloTreeNode child : children) {
            if (child.getVisitCount() == 0) {
                unexploredChildren.add(child);
            }
        }

        return unexploredChildren;
    }

    public boolean hasChildWithLossStatus() {
        for (MonteCarloTreeNode child : children) {
            if (child.status == Status.LOSS) {
                return true;
            }
        }

        return false;
    }
}
