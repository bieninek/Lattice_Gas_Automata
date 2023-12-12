package model;

import java.util.ArrayList;
import java.util.Collections;

// class represents one cell
public class Cell {
    // state is an arraylist of size 4, informs whether a particle is flowing
    // value 1 on 0th position means a particle coming to the upper cell
    // value 1 on 1st position means a particle coming to the right cell
    // value 1 on 2nd position means a particle coming to the down cell
    // value 1 on 3rd position means a particle coming to the left cell
    ArrayList<Integer> state;
    // type represents an info whether this cell is BORDER (black) or not
    final cellType type;

    public Cell(cellType type) {
        state = new ArrayList<>(Collections.nCopies(4, 0));
        this.type = type;
    }

    public ArrayList<Integer> getState() {
        return state;
    }

    public void setState(ArrayList<Integer> state) {
        this.state = state;
    }

    public cellType getType() {
        return type;
    }

    public enum cellType{
        BORDER,
        NORMAL
    }
}
