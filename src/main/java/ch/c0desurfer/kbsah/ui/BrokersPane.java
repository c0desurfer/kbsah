/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.ClusterNodesFetchTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.Node;

import java.io.IOException;
import java.util.Collection;

@Log4j2
public class BrokersPane extends ClusterContentPane {

    @FXML
    private Accordion accordion;

    public void showContent() {

        Task<Collection<Node>> fetchClusterNodes = new ClusterNodesFetchTask(getClusterName());

        fetchClusterNodes.setOnSucceeded(e -> {
            accordion.getPanes().clear();

            fetchClusterNodes.getValue().forEach(node -> {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/BrokerTitledPane.fxml"));
                try {
                    TitledPane titledPane = loader.load();

                    if (accordion.getPanes().isEmpty()) {
                        accordion.setExpandedPane(titledPane);
                    }

                    BrokerTitledPane brokerTitledPaneController = loader.getController();

                    brokerTitledPaneController.setBrokerId(node.idString());
                    brokerTitledPaneController.setClusterName(getClusterName());

                    titledPane.setText(node.idString() + ": " + node.host());
                    accordion.getPanes().add(titledPane);

                    brokerTitledPaneController.fetchBrokerConfiguration();

                } catch (IOException ioException) {
                    log.error("Loading broker titled pane into cluster tab pane failed.", ioException);
                }
            });
        });

        fetchClusterNodes.setOnFailed(workerStateEvent -> {
            log.warn("Fetching cluster nodes failed.", workerStateEvent.getSource().getException());
            accordion.getPanes().clear();
            TitledPane tpTemp = new TitledPane();
            tpTemp.setText("Fetching cluster nodes failed...");
            tpTemp.setContent(new Label(workerStateEvent.getSource().getException().toString()));
            accordion.getPanes().add(tpTemp);
            accordion.setExpandedPane(tpTemp);
        });

        ProgressIndicator progressIndicator = new ProgressIndicator();
        TitledPane tpTemp = new TitledPane();
        tpTemp.setText("Fetching cluster nodes...");
        tpTemp.setContent(progressIndicator);
        accordion.getPanes().add(tpTemp);

        accordion.setExpandedPane(tpTemp);

        Thread loadDataThread = new Thread(fetchClusterNodes);
        loadDataThread.start();
    }

}
