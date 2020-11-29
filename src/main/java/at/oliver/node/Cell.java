package at.oliver.node;

import at.oliver.heap.IndexInHeap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class Cell implements Comparable<Cell>, IndexInHeap, Cost {
    private NodeType type;
    private boolean isOpen, isClosed;
    private Point2D coordinates;
    private Cell explorer;
    private int g_cost,  // distance from starting node
            h_cost,  // distance from target node
            f_cost;  // g_cost + h_cost
    private int heapIndex;

    public Cell(NodeType type) {
        this.type = type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public boolean typeEquals(NodeType type) {
        return this.type == type;
    }

    public boolean isNotTraversable() {
        return this.typeEquals(NodeType.BARRIER);
    }

    public Color getColor() {
        return this.type.color;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void open() {
        if(this.isOpen()) {
            throw new IllegalStateException("Cell cannot be opened twice");
        }
        if(this.typeEquals(NodeType.BASIC)) {
            this.setType(NodeType.OPEN);
        }
        this.isOpen = true;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public void close() {
        assert !this.typeEquals(NodeType.TARGET);
        if(this.isClosed()) {
            throw new IllegalStateException("Cell cannot be closed twice");
        }
        if(!this.isOpen()) {
            throw new IllegalStateException("Cell must be open before being closed");
        }
        if(this.typeEquals(NodeType.OPEN)) {
            this.setType(NodeType.CLOSE);
        }
        this.isClosed = true;
    }

    public Point2D getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    public int getX() {
        return (int) this.coordinates.getX();
    }

    public int getY() {
        return (int) this.coordinates.getY();
    }

    public Cell getExplorer() {
        return explorer;
    }

    public void setExplorer(Cell explorer) {
        this.explorer = explorer;
    }

    @Override
    public int getG_cost() {
        return this.g_cost;
    }

    @Override
    public void setG_cost(int g_cost) {
        this.g_cost = g_cost;
        this.f_cost = this.g_cost + this.h_cost;
    }

    @Override
    public int getH_cost() {
        return this.h_cost;
    }

    @Override
    public void setH_cost(int h_cost) {
        this.h_cost = h_cost;
        this.f_cost = this.g_cost + this.h_cost;
    }

    @Override
    public int getF_cost() {
        return f_cost;
    }

    /**
     * Compares f_cost-values.
     *
     * @param that comparing Cell
     * @return compared f_cost, if f_cost is equal, h_cost will be compared
     */
    @Override
    public int compareTo(Cell that) {
        int result = Integer.compare(this.f_cost, that.f_cost);
        return result == 0 ? Integer.compare(this.h_cost, that.h_cost) : result;

        /*
        a negative int if this < that
        0 if this == that
        a positive int if this > that
        */
    }

    @Override
    public int getHeapIndex() {
        return this.heapIndex;
    }

    @Override
    public void setHeapIndex(int heapIndex) {
        this.heapIndex = heapIndex;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.type + ", g: " + this.g_cost + ", h: " + this.h_cost + ", f: " + this.f_cost + "]";
    }

    public enum NodeType {
        BASIC(Color.WHITE), START(Color.valueOf("299bc3")), TARGET(Color.valueOf("c329c0")), BARRIER(Color.BLACK), OPEN(Color.valueOf("6cbf03")), CLOSE(Color.valueOf("bf1306"));


        public final Color color;

        NodeType(Color color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return "CellType[color=" + this.color + "]";
        }
    }
}
