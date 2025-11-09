package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.ui.utils.TextAreaAppender;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

public class UtilitiesPane implements Initializable {

  @Setter
  private SplitPane splitPane;

  @Setter
  private MenuItem reopenUtilitiesView;

  @FXML
  private TextArea logTextArea;

  @FXML
  public void closeUtilities() {
    AnchorPane anchorPane = (AnchorPane) splitPane.lookup("#utilitiesPane");
    splitPane.getItems().remove(anchorPane);
    reopenUtilitiesView.setDisable(false);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextAreaAppender.setTextArea(logTextArea);
  }
}
