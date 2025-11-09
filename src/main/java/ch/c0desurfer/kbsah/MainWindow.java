/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah;

import ch.c0desurfer.kbsah.settings.Clusters;
import ch.c0desurfer.kbsah.ui.DialogUtils;
import ch.c0desurfer.kbsah.ui.UtilitiesPane;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;

@Log4j2
public class MainWindow implements Initializable {

  private final IntegerProperty kafkaClientTimeout = new SimpleIntegerProperty(30);
  private final StringProperty kafkaClientConsumerGroupId = new SimpleStringProperty("KAFKABROWSAH");
  private final StringProperty kafkaClientId = new SimpleStringProperty("KAFKABROWSAH");

  private final BooleanProperty openLogAtStartup = new SimpleBooleanProperty(false);

  private final PreferencesFx preferencesFx = PreferencesFx.of(Main.class,
      Category.of("Kafka",
          Group.of("Client",
              Setting.of("Timeout", kafkaClientTimeout),
              Setting.of("Consumer Group ID", kafkaClientConsumerGroupId),
              Setting.of("Client ID", kafkaClientId)
          )),
      Category.of("Utilities",
          Group.of("Log",
              Setting.of("Open Log At Startup", openLogAtStartup)
          ))
  );

  @FXML
  @Getter
  private AnchorPane contentPane;

  @FXML
  private AnchorPane utilitiesPane;

  @FXML
  private SplitPane splitPane;

  @FXML
  private MenuItem reopenUtilitiesView;

  @FXML
  private void openNewClusterDialog() {
    log.debug("Opening new cluster dialog.");

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/NewClusterWindow.fxml"));
      AnchorPane newKafkaClusterAnchorPane = loader.load();

      DialogUtils.displayDialog(newKafkaClusterAnchorPane, "New Kafka Cluster");
    } catch (IOException e) {
      log.error("Loading new kafka cluster dialog failed.", e);
    }
  }

  @FXML
  private void closeApplication() {
    Platform.exit();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    if (Boolean.TRUE.equals(openLogAtStartup.getValue())) {
      reopenUtilitiesView.setDisable(true);
      loadUtilitiesPane();
    } else {
      reopenUtilitiesView.setDisable(false);
      splitPane.getItems().remove(utilitiesPane);
    }

    if (!Clusters.builder().build().getClusters().isEmpty()) {
      loadContentWindow();
    }
    subscribeToClustersSettingsEvents();
  }

  private void loadContentWindow() {
    log.debug("There are clusters configured, opening content window.");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ContentWindow.fxml"));
    try {
      contentPane.getChildren().setAll((AnchorPane) loader.load());
    } catch (IOException e) {
      log.error("Loading content into content pane failed.", e);
    }
  }

  @FXML
  private void reopenUtilitiesView() {
    utilitiesPane = new AnchorPane();
    utilitiesPane.setId("utilitiesPane");

    splitPane.getItems().add(utilitiesPane);
    splitPane.setDividerPositions(0.8);

    loadUtilitiesPane();
  }

  private void loadUtilitiesPane() {
    log.debug("Loading utilities pane.");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UtilitiesPane.fxml"));

    try {
      utilitiesPane.getChildren().setAll((AnchorPane) loader.load());

      UtilitiesPane utilitiesPaneController = loader.getController();
      utilitiesPaneController.setSplitPane(splitPane);
      utilitiesPaneController.setReopenUtilitiesView(reopenUtilitiesView);

      reopenUtilitiesView.setDisable(true);
    } catch (IOException e) {
      log.error("Loading utilities pane failed.", e);
    }
  }

  private void subscribeToClustersSettingsEvents() {
    Clusters.builder().build().getPreferences().addNodeChangeListener(new NodeChangeListener() {
      @Override
      public void childAdded(NodeChangeEvent evt) {
        if (contentPane.getChildren().isEmpty()) {
          Platform.runLater(MainWindow.this::loadContentWindow);
        }
      }

      @Override
      public void childRemoved(NodeChangeEvent evt) {
        if (Clusters.builder().build().getClusters().isEmpty()) {
          Platform.runLater(() -> contentPane.getChildren().clear());
        }
      }
    });
  }

  public void openPreferences() {
    log.debug("Opening new preferences dialog.");
    preferencesFx.show(false);
  }
}
