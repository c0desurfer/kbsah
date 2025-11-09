package ch.c0desurfer.kbsah.ui.utils;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class FormsUtils {

    /**
     * Adjusts the column widths for any GridPane found within the specified Pane
     * hierarchy to match a desired label width percentage. This method recursively
     * traverses through the child nodes within the Pane, configuring column sizes
     * based on specific style class attributes and layout rules.
     *
     * @see <a href="https://github.com/dlsc-software-consulting-gmbh/FormsFX/issues/52">GitHub issue</a>
     *
     * @param pane The root Pane in which to search for GridPane instances.
     * @param labelSize The desired width percentage for label columns.
     */
    public static void searchAndSetControlsLabelWidth(Pane pane, double labelSize) {
        if (pane instanceof GridPane) {
            if (pane.getStyleClass().stream().anyMatch(s -> s.contains("simple-"))) {
                GridPane gp = (GridPane) pane;
                if (gp.getColumnConstraints().size() == 12) {
                    double rest = 100 - labelSize;
                    for (int i = 0; i < gp.getColumnConstraints().size(); i++) {
                        if (i < 3) {
                            gp.getColumnConstraints().get(i).setPercentWidth(labelSize / 2);
                        } else {
                            gp.getColumnConstraints().get(i).setPercentWidth(rest / 10);
                        }
                    }
                }
            }
        }

        for (Node child : pane.getChildren()) {
            if (child instanceof Pane cpane) {
                searchAndSetControlsLabelWidth(cpane, labelSize);
            }

            else if (child instanceof TitledPane tpane) {
                if (tpane.getContent() instanceof Pane cpane) {
                    searchAndSetControlsLabelWidth(cpane, labelSize);
                }
            }
        }
    }
}
