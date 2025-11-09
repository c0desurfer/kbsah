/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.ui.model.Cluster;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ContentWindow implements Initializable {

  @FXML
  private AnchorPane leftPane;

  @FXML
  private AnchorPane rightPane;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClustersListView.fxml"));
    try {
      ListView<Cluster> listView = loader.load();

      ClustersListView clustersListViewController = loader.getController();
      clustersListViewController.setRightPane(rightPane);
      clustersListViewController.setUp();

      leftPane.getChildren().setAll(listView);
    } catch (IOException e) {
      log.error("Loading accordion into left anchor pane failed.", e);
    }

  }
}
