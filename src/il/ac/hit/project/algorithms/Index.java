package il.ac.hit.project.algorithms;

import java.io.Serializable;
import java.util.Objects;

// represents a two dimensional index (row, col)
public class Index implements Serializable {
    public final int row, col;

    public Index(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // returns true if index is valid for matrix with specified number of rows and columns, else false
    public boolean isValid(int matrixNumRows, int matrixNumCols) {
        return row >= 0 && row < matrixNumRows && col >= 0 && col < matrixNumCols;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", row, col);
    }

    // equals and hashcode implemented so Index can be used in lists, maps and sets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return row == index.row &&
                col == index.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}