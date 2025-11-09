/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.AclsDescriptionTask;
import ch.c0desurfer.kbsah.ui.model.AclsSimpleTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

@Setter
@Log4j2
public class AclsPane extends ClusterContentPane implements Initializable {

    private ObservableList<AclsSimpleTable> filteredItems = FXCollections.observableArrayList();
    private ObservableList<AclsSimpleTable> unfilteredItems = FXCollections.observableArrayList();

    @FXML
    private TableView<AclsSimpleTable> tableView;

    @FXML
    private TextField nameFilter;

    @Override
    public void showContent() {

        Task<ObservableList<AclsSimpleTable>> fetchAclsTask = new AclsDescriptionTask(getClusterName());

        configureTableView();

        fetchAclsTask.setOnSucceeded(e -> {
            filteredItems = fetchAclsTask.getValue();
            unfilteredItems.addAll(filteredItems);

            if (!filteredItems.isEmpty()) {
                tableView.setItems(filteredItems);
                tableView.setPlaceholder(new Label("No ACLs to show."));
                tableView.getSelectionModel().select(0);
            }
        });

        fetchAclsTask.setOnFailed(workerStateEvent -> {
            log.warn("Could not list ACLs.", workerStateEvent.getSource().getException());
            tableView.setPlaceholder(new Label(workerStateEvent.getSource().getException().toString()));
        });

        ProgressIndicator progressIndicator = new ProgressIndicator();
        tableView.setPlaceholder(progressIndicator);

        Thread loadDataThread = new Thread(fetchAclsTask);
        loadDataThread.start();

    }

    private void configureTableView() {
        TableColumn<AclsSimpleTable, String> resourceTypeColumn = new TableColumn<>("Resource Type");
        TableColumn<AclsSimpleTable, String> resourceNameColumn = new TableColumn<>("Resource Name");
        TableColumn<AclsSimpleTable, String> patternTypeColumn = new TableColumn<>("Pattern Type");
        TableColumn<AclsSimpleTable, String> principalColumn = new TableColumn<>("Principal");
        TableColumn<AclsSimpleTable, String> hostColumn = new TableColumn<>("Host");
        TableColumn<AclsSimpleTable, String> operationColumn = new TableColumn<>("Operation");
        TableColumn<AclsSimpleTable, String> permissionTypeColumn = new TableColumn<>("Permission Type");

        resourceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceType"));
        resourceNameColumn.setCellValueFactory(new PropertyValueFactory<>("resourceName"));
        patternTypeColumn.setCellValueFactory(new PropertyValueFactory<>("patternType"));
        principalColumn.setCellValueFactory(new PropertyValueFactory<>("principal"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        operationColumn.setCellValueFactory(new PropertyValueFactory<>("operation"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getColumns().addAll(Arrays.asList(resourceNameColumn,
                resourceTypeColumn,
                patternTypeColumn,
                principalColumn,
                hostColumn,
                operationColumn,
                permissionTypeColumn));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameFilter.textProperty().addListener((observableValue, s, t1) -> updateFilteredData());
    }

    private void updateFilteredData() {
        filteredItems.clear();

        filteredItems.addAll(unfilteredItems.stream()
                .filter(aclsSimpleTable -> aclsSimpleTable.getResourceName()
                        .contains(nameFilter.getText())).toList());
    }
}
