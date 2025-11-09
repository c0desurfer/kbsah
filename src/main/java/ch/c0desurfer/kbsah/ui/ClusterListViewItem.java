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
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
public class ClusterListViewItem {

    private String clusterName;

    private MainTabPane mainTabPaneController;

    private AnchorPane anchorPane;

    @FXML
    private Label label;

    @FXML
    private Button connectButton;

    @FXML
    private ToggleButton toggleAutoConnectButton;

    @FXML
    public void connect() {
        mainTabPaneController.openNewClusterTab(clusterName);
    }

    @FXML
    private void toggleAutoConnect() {
        Clusters.builder().build().setClusterAutoConnect(clusterName, toggleAutoConnectButton.isSelected());
    }

    @FXML
    private void delete() {
        Clusters.builder().build().deleteCluster(clusterName);
    }

    @FXML
    private void edit() {
        log.debug("Opening edit cluster dialog.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditClusterWindow.fxml"));
            AnchorPane editKafkaClusterAnchorPane = loader.load();

            EditClusterWindow editClusterWindowController = loader.getController();
            editClusterWindowController.setClusterName(clusterName);
            editClusterWindowController.setOriginalClusterName(clusterName);
            editClusterWindowController.createForm();

            DialogUtils.displayDialog(editKafkaClusterAnchorPane, "Edit Kafka Cluster");

        } catch (IOException e) {
            log.error("Loading edit kafka cluster dialog failed.", e);
        }
    }

}
