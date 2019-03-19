package Michael_20;

import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoCoord;
import pentago_swap.PentagoMove;

public class CustomPentagoMove extends PentagoMove {


    public CustomPentagoMove(PentagoCoord coord, PentagoBoardState.Quadrant aSwap, PentagoBoardState.Quadrant bSwap, int playerId) {
        super(coord, aSwap, bSwap, playerId);
    }

    public CustomPentagoMove(int x, int y, PentagoBoardState.Quadrant aSwap, PentagoBoardState.Quadrant bSwap, int playerId) {
        super(x, y, aSwap, bSwap, playerId);
    }

    public CustomPentagoMove(String formatString) {
        super(formatString);
    }

    @Override
    public boolean equals(Object o) {

        if (o == null)
            return false;
        else if (o.getClass() != getClass() && o.getClass() != PentagoMove.class)
            return false;
        else if (o == this)
            return true;

        PentagoMove otherMove = (PentagoMove) o;

        return this.getASwap() == otherMove.getASwap() &&
                this.getBSwap() == otherMove.getBSwap() &&
                this.getMoveCoord().getX() == otherMove.getMoveCoord().getX() &&
                this.getMoveCoord().getY() == otherMove.getMoveCoord().getY();
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
