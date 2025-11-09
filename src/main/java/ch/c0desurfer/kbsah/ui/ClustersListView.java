/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.settings.Clusters;
import ch.c0desurfer.kbsah.ui.model.Cluster;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;

@Log4j2
@Setter
public class ClustersListView {

  private MainTabPane mainTabPaneController;

  private AnchorPane rightPane;

  @FXML
  private ListView<Cluster> listView;

  public void setUp() {
    log.debug("Initializing clusters list view.");

    if (Clusters.builder().build().getClusters().isEmpty()) {
      return;
    }

    log.debug("There are clusters configured, loading them into the list view.");

    listView.setCellFactory(clusterListView -> new CustomListCell());

    reloadClusters();
    loadMainTabPane();
    subscribeToPreferencesEvents();
  }

  private void loadMainTabPane() {
    log.debug("Loading main tab into right anchor pane.");

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainTabPane.fxml"));
    try {
      TabPane mainTabPane = loader.load();
      mainTabPaneController = loader.getController();
      rightPane.getChildren().addAll(mainTabPane);
    } catch (IOException e) {
      log.error("Loading main tab pane into right anchor pane failed.", e);
    }
  }

  private void subscribeToPreferencesEvents() {
    Clusters.builder().build().getPreferences().addNodeChangeListener(new NodeChangeListener() {
      @Override
      public void childAdded(NodeChangeEvent evt) {
        reloadClusters();
      }

      @Override
      public void childRemoved(NodeChangeEvent evt) {
        reloadClusters();
      }
    });
  }

  private void reloadClusters() {
    if (Clusters.builder().build().getClusters().isEmpty()) {
      return;
    }

    log.debug("Reloading clusters in list view.");

    Platform.runLater(() -> {
      listView.getItems().clear();
      listView.getItems().addAll(Clusters.builder().build().getClusters());
    });
  }

  private class CustomListCell extends ListCell<Cluster> {

    private final ClusterListViewItem clusterListViewItemController;
    private AnchorPane anchorPane;

    public CustomListCell() {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClusterListViewItem.fxml"));
      try {
        anchorPane = loader.load();
      } catch (IOException e) {
        log.error("Loading cluster view item failed.", e);
      }
      clusterListViewItemController = loader.getController();
    }

    @Override
    protected void updateItem(Cluster cluster, boolean empty) {
      super.updateItem(cluster, empty);

      if (cluster != null && !empty) {
        clusterListViewItemController.setMainTabPaneController(mainTabPaneController);
        clusterListViewItemController.setAnchorPane(rightPane);
        clusterListViewItemController.setClusterName(cluster.getClusterName().getValue());
        clusterListViewItemController.getLabel().setText(cluster.getClusterName().getValue());
        clusterListViewItemController.getToggleAutoConnectButton().setSelected(
            Clusters.builder().build().getClusterAutoConnect(
                cluster.getClusterName().getValue()));

        if (Clusters.builder().build().getClusterAutoConnect(cluster.getClusterName().getValue())) {
          clusterListViewItemController.connect();
        }

        setGraphic(anchorPane);
      } else {
        setGraphic(null);
      }
    }
  }
}
