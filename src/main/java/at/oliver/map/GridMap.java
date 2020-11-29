package at.oliver.map;

import at.oliver.Controller;
import at.oliver.heap.MinHeap;
import at.oliver.node.Cell;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

import static at.oliver.node.Cell.NodeType;

/**
 * A {@code GridMap} is a gridded {@code Canvas} with one start and end point,
 * and a variable number of barriers. Used for visualizing a path-finding-
 * algorithm.
 *
 * @author Oliver Sommer
 * @see Canvas
 */
public class GridMap extends HBox implements EventHandler<Event> {
    private static final int GRID_SIZE_MIN = 6;
    private static final int GRID_SIZE_MAX = 40;
    private static final Color PATH_COLOR = Color.valueOf("7662c2");

    private final Canvas map;
    private final GraphicsContext gc;

    private Cell start = new Cell(NodeType.START);
    private Cell target = new Cell(NodeType.TARGET);
    private NodeType selectedNodeType;
    private final ChangeListener<Boolean> buttonFocusListener = (observableValue, oldValue, newValue) -> {
        if(!newValue) {  // no button is selected, nothing will be drawn
            this.setSelectedNodeType(null);
        }
    };
    private Cell[][] grid;  // stores state of all cells
    private int gridSize;
    private double cellSize;
    private Controller controller;
    private final EventHandler<ActionEvent> buttonListener = actionEvent -> {  // buttons for drawing on grid
        assert actionEvent.getSource() instanceof Button;
        this.setSelectedNodeType(this.controller.getSource((Button) actionEvent.getSource()));
    };
    private int delay;  // in milliseconds

    // initializer
    {
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Constructs and initializes a gridded, white {@code GridMap}. Grid size is 10 by default.
     *
     * @param size width and height of the {@code GridMap}
     * @see Canvas
     */
    public GridMap(@NamedArg("size") int size) {
        this(size, 10);
    }

    /**
     * Constructs and initializes a gridded, white {@code GridMap}.
     *
     * @param size     width and height of the {@code GridMap}
     * @param gridSize amount of rows and columns to be drawn, min: {@value GRID_SIZE_MIN}, max: {@value GRID_SIZE_MAX}
     * @see Canvas
     */
    public GridMap(@NamedArg("size") int size, @NamedArg("gridSize") int gridSize) {
        this.map = new Canvas(size, size);
        this.getChildren().add(this.map);

        this.map.setOnMousePressed(this);
        this.map.setOnMouseDragged(this);
        this.map.setOnScroll(this);

        this.gc = this.map.getGraphicsContext2D();
        this.gc.setStroke(Color.valueOf("161616"));

        this.setGridSize(gridSize);
    }

    /**
     * Calculates the distance between two points by only traveling either crosswise or straight across a {@code Cell}
     *
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @return distance between the two points
     */
    private static int getDistance(int x1, int y1, int x2, int y2) {
        int diffX = Math.abs(x1 - x2);
        int diffY = Math.abs(y1 - y2);

        return diffX > diffY ? 14 * diffY + 10 * (diffX - diffY) : 14 * diffX + 10 * (diffY - diffX);
    }

    public int getGridSize() {
        return this.gridSize;
    }

    public void setGridSize(int size) {
        if(size < GRID_SIZE_MIN || size > GRID_SIZE_MAX) {
            throw new IllegalArgumentException("Error at Map: gridSize must be within range " + GRID_SIZE_MIN + " to " + GRID_SIZE_MAX);
        }

        this.gridSize = size;

        grid = new Cell[this.gridSize][this.gridSize];

        this.clearCanvas();
        this.drawGrid();
    }

    /**
     * Clears the {@code Canvas} and attributes
     */
    private void clearCanvas() {
        this.gc.clearRect(0, 0, this.map.getWidth(), this.map.getHeight());

        this.start.setCoordinates(null);
        this.target.setCoordinates(null);

        for(int i = 0; i < this.gridSize; i++) {
            for(int j = 0; j < this.gridSize; j++) {
                this.grid[i][j] = new Cell(NodeType.BASIC);
            }
        }

        this.start = new Cell(NodeType.START);
        this.target = new Cell(NodeType.TARGET);
    }

    /**
     * Draws a quadratic grid with side length according to the gridSize
     *
     * @see GridMap#gridSize
     */
    private void drawGrid() {
        // size was tested via EventHandler or internally
        assert this.gridSize >= GRID_SIZE_MIN;
        assert this.gridSize <= GRID_SIZE_MAX;

        double increment = this.map.getWidth() / this.gridSize;

        this.gc.setLineWidth(increment / 8);

        // height and width must be equal, tested in initializer
        for(double x = 0; x < this.map.getWidth() + this.gc.getLineWidth(); x += increment) {
            for(double y = 0; y < this.map.getHeight() + this.gc.getLineWidth(); y += increment) {
                this.gc.strokeLine(x, y, x, y + this.map.getWidth());
                this.gc.strokeLine(x, y, x + this.map.getWidth(), y);
            }
        }
        this.cellSize = increment;
    }

    /**
     * Paints the cell on the {@code GridMap} and sets its type to the given parameter.
     *
     * @param type type of the node, which it will be set to
     */
    public void paintCell(Cell cell, NodeType type, boolean changeType) {
        this.paintSquare(cell.getX(), cell.getY(), type.color);

        if(changeType) {
            this.grid[cell.getX()][cell.getY()].setType(type);
        }
    }

    /**
     * Paints the cell on the {@code GridMap} to its attribute color
     *
     * @param cell cell to be painted
     */
    private void paintCell(Cell cell) {
        this.paintCell(cell, cell.getColor());
    }

    /**
     * For opened or closed nodes
     *
     * @param cell  focused cell
     * @param color color to be drawn
     */
    private void paintCell(Cell cell, Color color) {  // for opened or closed nodes
        assert cell.getF_cost() > 0;

        if(!this.controller.onlyShowPathItem.isSelected() || color.equals(GridMap.PATH_COLOR)) {
            GridMap.this.paintSquare(cell.getX(), cell.getY(), color);
            if(GridMap.this.controller.showValuesItem.isSelected()) {  // drawing cost of cells if menu item was selected
                GridMap.this.gc.setFill(Color.valueOf("161616"));
                GridMap.this.gc.setFont(new Font((int) (GridMap.this.cellSize * 4 / 10)));
                GridMap.this.gc.setTextAlign(TextAlignment.CENTER);
                GridMap.this.gc.fillText(String.valueOf(cell.getF_cost()),  // center
                        cell.getX() * cellSize + (cellSize / 2), cell.getY() * cellSize + (cellSize / 4) * 3);
                GridMap.this.gc.setFont(new Font((int) (GridMap.this.cellSize * 2 / 10)));
                GridMap.this.gc.setTextAlign(TextAlignment.LEFT);
                GridMap.this.gc.fillText(String.valueOf(cell.getG_cost()),  // top left
                        cell.getX() * cellSize + (cellSize / 8), cell.getY() * cellSize + (cellSize / 4));
                GridMap.this.gc.setTextAlign(TextAlignment.RIGHT);
                GridMap.this.gc.fillText(String.valueOf(cell.getH_cost()),  // top right
                        (cell.getX() + 1) * cellSize - (cellSize / 8), cell.getY() * cellSize + (cellSize / 4));
            }
        }
    }

    /**
     * Fills the square at x, y with given color
     *
     * @param x     x-position
     * @param y     y-position
     * @param color color to be filled
     */
    private void paintSquare(int x, int y, Color color) {
        this.gc.setFill(color);

        double width = this.gc.getLineWidth();

        this.gc.fillRect(x * cellSize + width / 2, y * cellSize + width / 2, cellSize - width, cellSize - width);
    }

    /**
     * Calculates the position in the grid by dividing the {@param coordinate} by the size of the square.
     *
     * @param coordinate x- or y-coordinate
     * @return the position of the coordinate in the grid
     */
    public int coordinateToGridPos(double coordinate) {
        return (int) (coordinate / this.cellSize);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Draws the path from start to target in violet
     *
     * @param cells List of the cells which lie on the path
     */
    public void drawPath(List<Cell> cells) {
        for(Cell cell : cells) {
            this.paintCell(cell, GridMap.PATH_COLOR);  // violet
        }

        Controller.disable(false, GridMap.this.controller.reset);
    }

    private List<Cell> getPath(Cell cell) {
        List<Cell> path = new ArrayList<>();

        cell = cell.getExplorer();
        Cell explorer = cell.getExplorer();
        while(explorer != null) {  // until startPoint (which doesn't have an explorer)
            path.add(cell);
            cell = explorer;
            explorer = cell.getExplorer();
        }
        //Collections.reverse(path);
        return path;
    }

    /**
     * Draws "No Path" on the {@code Canvas}
     */
    public void drawTextNoExistingPath() {
        this.gc.setFont(new Font(this.map.getWidth() / 5));
        this.gc.setFill(Color.valueOf("9c9c9c"));
        this.gc.setTextAlign(TextAlignment.CENTER);
        this.gc.fillText("No Path", this.map.getWidth() / 2, this.map.getHeight() / 5 * 3);
    }

    @Override
    public void handle(Event event) {
        if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED) || event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            this.handleMouseEvent((MouseEvent) event);
        }
        else if(event.getEventType().equals(ScrollEvent.SCROLL)) {
            this.handleScrollEvent((ScrollEvent) event);
        }
    }

    /**
     * Handles mouse events: paints nodes on the grid accordingly to the selected type of cell
     *
     * @param event MouseEvent.MOUSE_PRESSED or MouseEvent.MOUSE_DRAGGED
     * @see Cell.NodeType
     * @see GridMap#selectedNodeType
     */
    private void handleMouseEvent(MouseEvent event) {
        if(this.getSelectedNodeType() == null || !this.map.contains(event.getX(), event.getY())) {
            return;
        }

        int x = this.coordinateToGridPos(event.getX());
        int y = this.coordinateToGridPos(event.getY());

        if(grid[x][y].typeEquals(NodeType.START)) {
            this.start.setCoordinates(null);
            Controller.disable(true, this.controller.run);
        }
        else if(grid[x][y].typeEquals(NodeType.TARGET)) {
            this.target.setCoordinates(null);
            Controller.disable(true, this.controller.run);
        }

        if(grid[x][y].typeEquals(NodeType.START)) {
            this.start.setCoordinates(null);
        }

        if(this.getSelectedNodeType().equals(NodeType.START)) {  // painting start button
            if(this.start.getCoordinates() != null)  // removing previous start
            {
                this.paintCell(start, NodeType.BASIC, true);
            }
            this.start.setCoordinates(new Point2D(x, y));
            if(target.getCoordinates() != null) {
                Controller.disable(false, this.controller.run);
            }
        }
        else if(this.getSelectedNodeType().equals(NodeType.TARGET)) {  // painting target button
            if(this.target.getCoordinates() != null)  // removing previous target
            {
                this.paintCell(target, NodeType.BASIC, true);
            }
            this.target.setCoordinates(new Point2D(x, y));
            if(start.getCoordinates() != null) {
                Controller.disable(false, this.controller.run);
            }
        }

        this.grid[x][y].setCoordinates(new Point2D(x, y));

        this.paintCell(grid[x][y], this.getSelectedNodeType(), true);
    }

    public NodeType getSelectedNodeType() {
        return this.selectedNodeType;
    }

    public void setSelectedNodeType(NodeType type) {
        this.selectedNodeType = type;

        this.map.setCursor((selectedNodeType == null) ? Cursor.DEFAULT : Cursor.CROSSHAIR);
    }

    /**
     * Handles scrolling, changes the size of the grid.
     *
     * @param event MouseEvent.SCROLL
     */
    private void handleScrollEvent(ScrollEvent event) {
        int amountScrolled = Integer.compare(0, (int) event.getDeltaY()) * 2;  // checks if zoomed in or out

        if(this.gridSize + amountScrolled < GRID_SIZE_MIN || this.gridSize + amountScrolled > GRID_SIZE_MAX) {
            return;
        }

        this.setGridSize(this.gridSize + amountScrolled);  // provides method with changed value

        Controller.disable(true, this.controller.run);
    }

    public EventHandler<ActionEvent> getButtonListener() {
        return this.buttonListener;
    }

    public ChangeListener<Boolean> getButtonFocusListener() {
        return this.buttonFocusListener;
    }

    /**
     * Returns the G_cost (distance between the start cell and given cell)
     *
     * @param explorerCell explorer of focused-cell
     * @param focusedCell  currently focused cell
     * @return g_cost
     */
    public int calcG_cost(Cell explorerCell, Cell focusedCell) {
        // not using explorer of current (Point2D(cX, CY)) for testing for shorter paths via other explorers
        return explorerCell.getG_cost() + GridMap.getDistance(explorerCell.getX(), explorerCell.getY(), focusedCell.getX(), focusedCell.getY());
    }

    /**
     * Returns the H_cost (distance between given cell and target Point)
     *
     * @param focusedCell currently focused cell
     * @return g_cost
     */
    public int calcH_cost(Cell focusedCell) {
        assert this.target != null;
        return GridMap.getDistance(focusedCell.getX(), focusedCell.getY(), target.getX(), target.getY());
    }


    /**
     * Initializes and starts algorithm
     */
    public void startAlgorithm() {
        assert GridMap.this.start.getCoordinates() != null;
        assert GridMap.this.target.getCoordinates() != null;

        new Algorithm().start();
    }

    /**
     * Sets delay to be waited in the {@code Algorithm}. Visual purpose.
     *
     * @param delay in milliseconds
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Algorithm for finding the shortest path between the start and the target.
     * Uses A* search algorithm.
     *
     * @see Algorithm#run
     */
    class Algorithm extends Thread {
        @Override
        public void run() {
            Cell[][] grid = GridMap.this.grid;

            MinHeap<Cell> open = new MinHeap<>(GridMap.this.gridSize * GridMap.this.gridSize);

            GridMap.this.start.setG_cost(0);  // 0 distance to the start
            GridMap.this.start.setH_cost(GridMap.this.calcH_cost(GridMap.this.start));

            open.add(GridMap.this.start);
            GridMap.this.start.open();

            if(GridMap.this.controller.selfExploreItem.isSelected()) {
                this.runInteractively(open, grid);
            }
            else {
                this.runAStarAlgorithm(open, grid);
            }
        }

        private void runInteractively(MinHeap<Cell> open, Cell[][] grid) {  // when self-exploring-mode is selected
            this.exploreNeighbours(open, grid, GridMap.this.start);

            GridMap.this.map.setOnMouseClicked(event -> {
                int x = GridMap.this.coordinateToGridPos(event.getX());
                int y = GridMap.this.coordinateToGridPos(event.getY());

                if(!grid[x][y].isOpen() || grid[x][y].isClosed()) {
                    return;
                }

                Cell current = grid[x][y];  // get mouse coordinates
                //open.remove(grid[x][y]);

                if(!current.typeEquals(NodeType.TARGET)) {
                    this.exploreNeighbours(open, grid, current);

                    if(open.size() == 0) {  // no path found
                        GridMap.this.drawTextNoExistingPath();
                        GridMap.this.map.setOnMouseClicked(GridMap.this);  // removes this EventHandler
                    }
                }
                else {  // finished
                    GridMap.this.drawPath(GridMap.this.getPath(current));
                    GridMap.this.map.setOnMouseClicked(GridMap.this);
                }
            });
        }

        private void runAStarAlgorithm(MinHeap<Cell> open, Cell[][] grid) {
            assert open.size() > 0;

            long start = System.currentTimeMillis();
            int time = 0;

            while(open.size() > 0) {
                Cell current = open.removeFirst();  // returns item with lowest f_cost

                if(current.typeEquals(NodeType.TARGET)) {  // finished
                    GridMap.this.drawPath(GridMap.this.getPath(current));
                    System.out.println("Finished in " + (System.currentTimeMillis() - start - time) + "ms (without delay)");
                    return;
                }

                this.exploreNeighbours(open, grid, current);

                this.sleepDelay();
                time += GridMap.this.delay;
//                System.out.println(time + ", inc: " + delay);
            }
            // no path found
            GridMap.this.drawTextNoExistingPath();
            Controller.disable(false, GridMap.this.controller.reset);
        }

        /**
         * Explores the adjacent cells around a given cell explorer.
         *
         * @param open     Heap of opened cells
         * @param grid     2D array of cells
         * @param explorer center cell; adjacent cells will be explored
         */
        private void exploreNeighbours(MinHeap<Cell> open, Cell[][] grid, Cell explorer) {
            explorer.close();
            GridMap.this.paintCell(explorer);

            // explore neighbours and search for shorter paths
            for(Cell neighbour : this.getNeighbours(grid, explorer)) {

                // testing for smaller g_cost via a new route
                int newCost = GridMap.this.calcG_cost(explorer, neighbour);
                if(newCost < neighbour.getG_cost() || neighbour.getG_cost() == 0) {
                    neighbour.setG_cost(newCost);
                    neighbour.setExplorer(explorer);

                    if(!neighbour.isOpen()) {
                        neighbour.setH_cost(GridMap.this.calcH_cost(neighbour));

                        open.add(neighbour);
                        neighbour.open();
                    }
                    else  // opened
                    {
                        open.updateItem(neighbour);  // cost has changed -> position in heap might too
                    }
                }

                GridMap.this.paintCell(neighbour);
            }
        }

        /**
         * Returns the adjacent cells of a given cell explorer.
         *
         * @param grid     2D array of cells
         * @param explorer center cell; adjacent cells will be explored
         * @return {@code List<Cell>} of the neighbours
         */
        private List<Cell> getNeighbours(Cell[][] grid, Cell explorer) {
            List<Cell> neighbours = new ArrayList<>();
            for(int x = explorer.getX() - 1; x <= explorer.getX() + 1; x++) {
                for(int y = explorer.getY() - 1; y <= explorer.getY() + 1; y++) {
                    if(x < 0 || y < 0 || x >= GridMap.this.gridSize || y >= GridMap.this.gridSize || grid[x][y] == explorer || grid[x][y].isClosed() || grid[x][y].isNotTraversable() || grid[x][y].typeEquals(NodeType.START)) {
                        continue;
                    }
                    grid[x][y].setCoordinates(new Point2D(x, y));
                    neighbours.add(grid[x][y]);
                }
            }
            return neighbours;
        }

        /**
         * Lets the thread sleep for the duration of the attribute delay in {@code GridMap}
         *
         * @see Thread#sleep
         * @see GridMap#delay
         */
        private void sleepDelay() {
            try {
                Thread.sleep(GridMap.this.delay);
            }
            catch(InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
