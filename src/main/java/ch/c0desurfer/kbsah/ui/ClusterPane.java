/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Setter
@Log4j2
public class ClusterPane {
  private String clusterName;

  private ClusterContentPane paneController;

  @FXML
  private AnchorPane clusterPane;

  public void addPanes() {
    clusterPane.getChildren().add(loadAnchorPane());
  }

  private AnchorPane loadAnchorPane() {
    FXMLLoader paneLoader = new FXMLLoader(getClass().getResource("/" + "TopicsPane" + ".fxml"));

    try {

      AnchorPane anchorPane = paneLoader.load();
      paneController = paneLoader.getController();

      paneController.setClusterName(clusterName);

      paneController.showContent();

      return anchorPane;

    } catch (IOException e) {
      log.error("Loading fxml {} into cluster tab pane failed.", "TopicsPane", e);
    }

    return null;
  }
}
