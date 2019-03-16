package student_player;

import pentago_swap.PentagoMove;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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

    private Status status;

    public enum Status {
        WON, LOSS, PROGRESS
    }

    public MonteCarloTreeNode() {
        parent = null;
        children = new LinkedList<>();
        move = null;
        status = Status.PROGRESS;
    }

    public MonteCarloTreeNode(MonteCarloTreeNode parent, PentagoMove move) {
        this.parent = parent;
        this.move = move;
        children = new LinkedList<>();
        parent.addChild(this);
        status = Status.PROGRESS;
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

    public void win() {
        winRatioUpToDate = false;
        winCount++;
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
        if (parent == null) {
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
}
