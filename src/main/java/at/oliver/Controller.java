package at.oliver;

import at.oliver.map.GridMap;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;

import java.util.Arrays;

import static at.oliver.node.Cell.NodeType;

public class Controller {
    @FXML
    public CheckMenuItem selfExploreItem, showValuesItem, onlyShowPathItem;

    // toggle buttons
    @FXML
    public Button startButton, targetButton, barrierButton, eraseButton;

    // button
    @FXML
    public Button run, reset;
    @FXML
    BorderPane pane;

    // delay-slider
    @FXML
    private Slider delaySlider;
    @FXML
    private Label delayLabel;

    @FXML
    private GridMap gridMap;

    private final EventHandler<ActionEvent> onlyShowPathListener = actionEvent -> {
        if(this.onlyShowPathItem.isSelected()) {
            this.delaySlider.setValue(0);
            this.refreshDelayLabel();
            this.delaySlider.setDisable(true);
        }
        else {
            this.delaySlider.setDisable(false);
        }
    };
    private final ChangeListener<Number> sliderListener = (observableValue, oldValue, newValue) -> this.refreshDelayLabel();

    /**
     * Disables all given {@code nodes}
     *
     * @param bool  {@code true} will disable the nodes, {@code} false will enable the nodes
     * @param nodes JavaFX nodes
     */
    public static void disable(boolean bool, Node... nodes) {
        for(Node node : nodes) {
            node.setDisable(bool);
        }
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    private void refreshDelayLabel() {
        this.delayLabel.setText((int) this.delaySlider.getValue() + "ms");
        this.gridMap.setDelay((int) this.delaySlider.getValue());
    }

    @FXML
    private void runClicked() {
        this.disableDrawingButtons(true);
        Controller.disable(true, run, reset);
        this.gridMap.startAlgorithm();
    }

    @FXML
    private void resetClicked() {
        this.gridMap.setGridSize(this.gridMap.getGridSize());
        this.disableDrawingButtons(false);
        Controller.disable(true, this.run);
    }

    @FXML
    public void initialize() {
        for(Button button : Arrays.asList(startButton, targetButton, barrierButton, eraseButton)) {
            button.setOnAction(gridMap.getButtonListener());
            button.focusedProperty().addListener(gridMap.getButtonFocusListener());
        }

        this.onlyShowPathItem.setOnAction(onlyShowPathListener);

        this.delaySlider.valueProperty().addListener(sliderListener);
        this.refreshDelayLabel();

        Controller.disable(true, this.run);

        this.gridMap.setController(this);
    }

    public void disableDrawingButtons(boolean bool) {
        Controller.disable(bool, startButton, targetButton, barrierButton, eraseButton);
    }

    public NodeType getSource(Button b) {
        NodeType type;
        if(b == barrierButton)  // barrier
        {
            type = NodeType.BARRIER;
        }
        else if(b == eraseButton)  //erase
        {
            type = NodeType.BASIC;
        }
        else if(b == startButton)  // start
        {
            type = NodeType.START;
        }
        else /* if(b == targetButton) */  // target
        {
            type = NodeType.TARGET;
        }
        return type;
    }
}
