/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.BrokerDescriptionTask;
import ch.c0desurfer.kbsah.ui.model.BrokerSimpleTable;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
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

@Setter
@Log4j2
public class BrokerTitledPane implements Initializable {

    private String clusterName;
    private String brokerId;

    private ObservableList<BrokerSimpleTable> filteredItems = FXCollections.observableArrayList();
    private ObservableList<BrokerSimpleTable> unfilteredItems = FXCollections.observableArrayList();

    @FXML
    private TextField nameFilter;

    @FXML
    private TableView<BrokerSimpleTable> tableView;

    public void fetchBrokerConfiguration() {
        Task<ObservableList<BrokerSimpleTable>> fetchBrokerTask = new BrokerDescriptionTask(clusterName, brokerId);

        configureTableView();

        fetchBrokerTask.setOnSucceeded(e -> {
            filteredItems = fetchBrokerTask.getValue();
            unfilteredItems.addAll(filteredItems);

            tableView.setItems(filteredItems);
        });

        fetchBrokerTask.setOnFailed(workerStateEvent -> {
            log.warn("Could not get broker description.", workerStateEvent.getSource().getException());
            tableView.setPlaceholder(new Label(workerStateEvent.getSource().getException().toString()));
        });

        ProgressIndicator progressIndicator = new ProgressIndicator();
        tableView.setPlaceholder(progressIndicator);

        Thread loadDataThread = new Thread(fetchBrokerTask);
        loadDataThread.start();

    }

    private void configureTableView() {
        TableColumn<BrokerSimpleTable, String> nameColumn = new TableColumn<>("Name");
        TableColumn<BrokerSimpleTable, String> valueColumn = new TableColumn<>("Value");
        TableColumn<BrokerSimpleTable, String> sourceColumn = new TableColumn<>("Source");
        TableColumn<BrokerSimpleTable, String> defaultColumn = new TableColumn<>("Default");
        TableColumn<BrokerSimpleTable, String> readOnlyColumn = new TableColumn<>("Read Only");
        TableColumn<BrokerSimpleTable, String> sensitiveColumn = new TableColumn<>("Sensitive");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        defaultColumn.setCellValueFactory(new PropertyValueFactory<>("default"));
        readOnlyColumn.setCellValueFactory(new PropertyValueFactory<>("readOnly"));
        sensitiveColumn.setCellValueFactory(new PropertyValueFactory<>("sensitive"));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getColumns().addAll(Arrays.asList(nameColumn, valueColumn, sourceColumn, defaultColumn, readOnlyColumn, sensitiveColumn));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameFilter.textProperty().addListener((observableValue, s, t1) -> updateFilteredData());
    }

    private void updateFilteredData() {
        filteredItems.clear();

        filteredItems.addAll(unfilteredItems.stream()
                .filter(brokerSimpleTable -> brokerSimpleTable.getName()
                        .contains(nameFilter.getText()))
                .toList());
    }
}
