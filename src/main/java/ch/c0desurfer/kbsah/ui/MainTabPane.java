/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class MainTabPane {

  @FXML
  private TabPane mainTabPane;

  public void openNewClusterTab(String clusterName) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClusterTabPane.fxml"));
    try {
      TabPane clusterTabPane = loader.load();

      ClusterTabPane clusterTabPaneController = loader.getController();

      clusterTabPaneController.setClusterName(clusterName);

      clusterTabPaneController.addPanes();

      Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/kafka-icon-16.png")));
      ImageView imageView = new ImageView(image);

      imageView.setFitHeight(16.0);
      imageView.setFitWidth(16.0);

      Tab tab = new Tab(clusterName, clusterTabPane);
      tab.setGraphic(imageView);

      tab.setId(clusterName);

      mainTabPane.getTabs().add(tab);
      mainTabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      log.error("Loading cluster tab pane into main tab pane failed.", e);
    }
  }
}
