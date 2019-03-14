package student_player;

import pentago_swap.PentagoMove;

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

    public MonteCarloTreeNode() {
        parent = null;
        children = new LinkedList<>();
        move = null;
    }

    public MonteCarloTreeNode(MonteCarloTreeNode parent, PentagoMove move) {
        this.parent = parent;
        this.move = move;
        children = new LinkedList<>();
        parent.addChild(this);
    }

    public double getWinRatio() {
        if (!winRatioUpToDate) {
            winRatio = ((double) winCount) / visitCount;
            winRatioUpToDate = true;
        }
        return winRatio;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public void visit() {
        visitCount++;
    }

    public void win() {
        winCount++;
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
        if (visitCount == 0) {
            return Integer.MAX_VALUE;
        }
        if (parent == null) {
            return 0;
        }
        return this.getWinRatio() + 1.41 * Math.sqrt(Math.log(parent.getVisitCount()) / (double) visitCount);
    }
}
