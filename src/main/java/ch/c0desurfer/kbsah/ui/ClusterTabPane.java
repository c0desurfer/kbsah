/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Setter
@Log4j2
public class ClusterTabPane {
    private String clusterName;

    @FXML
    private TabPane clusterTabPane;

    public void addPanes() {
        Tab topicsTab = new Tab("Topics", loadAnchorPane("TopicsPane"));
        topicsTab.setClosable(false);

        Tab aclsTab = new Tab("ACLs", loadAnchorPane("AclsPane"));
        aclsTab.setClosable(false);

        Tab clusterTab = new Tab("Cluster", loadAnchorPane("BrokersPane"));
        clusterTab.setClosable(false);

        clusterTabPane.getTabs().add(topicsTab);
        clusterTabPane.getTabs().add(aclsTab);
        clusterTabPane.getTabs().add(clusterTab);
    }

    private AnchorPane loadAnchorPane(String fxml) {
        FXMLLoader paneLoader = new FXMLLoader(getClass().getResource("/" + fxml + ".fxml"));

        try {

            AnchorPane anchorPane = paneLoader.load();
            ClusterContentPane paneController = paneLoader.getController();

            paneController.setClusterName(clusterName);

            paneController.showContent();

            return anchorPane;

        } catch (IOException e) {
            log.error("Loading fxml {} into cluster tab pane failed.", fxml, e);
        }

        return null;
    }
}
