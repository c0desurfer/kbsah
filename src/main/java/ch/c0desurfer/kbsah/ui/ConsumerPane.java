/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.KafkaRecordConsumer;
import ch.c0desurfer.kbsah.ui.model.ConsumerRecordSimple;
import ch.c0desurfer.kbsah.ui.utils.RecordFormatter;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

@Log4j2
@Setter
public class ConsumerPane implements Initializable {

    private String clusterName;
    private String topicName;

    private ObservableList<ConsumerRecordSimple> records = FXCollections.observableArrayList();

    private boolean subscribing;

    @FXML
    private ChoiceBox<String> startPointChoice;

    @FXML
    private Button consumeButton;

    @FXML
    private TableView<ConsumerRecordSimple> tableView;

    @FXML
    private WebView webView;

    @FXML
    private TextField limitTextField;

    private KafkaRecordConsumer kafkaRecordConsumer;

    private ProgressIndicator pi = new ProgressIndicator();

    @FXML
    private void clickedButton() {
        toggleSubscribing();
    }

    private void toggleSubscribing() {
        if (!subscribing) {
            subscribeToFxElementsEvents();
            clearTableView();

            int limit = 50;
            if (!StringUtils.isBlank(limitTextField.getText())
                && StringUtils.isNumeric(limitTextField.getText())) {
                try {
                    limit = Integer.parseInt(limitTextField.getText());
                } catch (NumberFormatException e) {
                    log.warn("Limit is not a number. Ignoring.");
                }
            }

            log.debug("Limit is {}.", limit);

            kafkaRecordConsumer = new KafkaRecordConsumer(clusterName, topicName, startPointChoice.getValue(), limit);
            kafkaRecordConsumer.getRecords().addListener(new RecordListChangeListener());
            kafkaRecordConsumer.getShutdown().addListener(new ShutdownListener());
            kafkaRecordConsumer.start();

            toggleConsumingButton();
        } else {
            unsubscribeAndShutdown();
        }
    }

    public void unsubscribeAndShutdown() {
        if (subscribing) {
            unsubscribeFromFxElementsEvents();
            kafkaRecordConsumer.getShutdown().set(true);
        }
    }

    private void toggleConsumingButton() {
        subscribing = !subscribing;

        if (consumeButton.getText().equals("Consume")) {
            Platform.runLater(() -> {
                consumeButton.setText("Stop Consuming");
                consumeButton.setGraphic(pi);
            });
        } else {
            Platform.runLater(() -> {
                consumeButton.setText("Consume");
                consumeButton.setGraphic(null);
            });
        }
    }

    private class ShutdownListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
            log.debug("Shutdown listener received event.");
            toggleConsumingButton();
        }
    }

    private class RecordListChangeListener implements ListChangeListener<ConsumerRecordSimple> {

        @Override
        public void onChanged(Change<? extends ConsumerRecordSimple> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    log.debug("Record added event.");
                    Platform.runLater(() -> records.addAll(change.getAddedSubList()));
                }
            }
        }
    }

    private void clearTableView() {
        webView.getEngine().loadContent("");
        records.removeAll();
        tableView.getItems().clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        consumeButton.setContentDisplay(ContentDisplay.RIGHT);

        pi.setPrefHeight(10);
        pi.setPrefWidth(10);

        startPointChoice.getItems().add("Now");
        startPointChoice.getItems().add("Last Hour");
        startPointChoice.getItems().add("Today");
        startPointChoice.getItems().add("Yesterday");
        startPointChoice.getItems().add("From Beginning");
        startPointChoice.setValue("Now");

        initializeTable();

        Platform.runLater(this::toggleSubscribing);
    }

    private void initializeTable() {
        TableColumn<ConsumerRecordSimple, String> keyTableColumn = new TableColumn<>("Key");
        TableColumn<ConsumerRecordSimple, String> offsetTableColumn = new TableColumn<>("Offset");
        TableColumn<ConsumerRecordSimple, String> partitionTableColumn = new TableColumn<>("Partition");
        TableColumn<ConsumerRecordSimple, String> timestampTableColumn = new TableColumn<>("Timestamp");

        keyTableColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        offsetTableColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));
        partitionTableColumn.setCellValueFactory(new PropertyValueFactory<>("partition"));
        timestampTableColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getColumns().addAll(Arrays.asList(keyTableColumn, offsetTableColumn, partitionTableColumn, timestampTableColumn));
        tableView.setPlaceholder(new Label("No records to show."));

        tableView.getSelectionModel().selectedItemProperty().addListener(new CustomSelectListener());

        tableView.setItems(records);
    }

    private Optional<Tab> getCurrentTab() {
        return Optional.ofNullable(consumeButton.getScene().lookup("#mainTabPane"))
            .filter(TabPane.class::isInstance)
            .map(TabPane.class::cast)
            .flatMap(mainTabPane -> mainTabPane.getTabs().stream()
                .filter(tab -> isDescendant(tab.getContent(), consumeButton))
                .findFirst());
    }

    private boolean isDescendant(Node parent, Node descendant) {
        return Stream.iterate(descendant, Objects::nonNull, Node::getParent)
            .anyMatch(node -> node == parent);
    }

    private void subscribeToFxElementsEvents() {
        getCurrentTab().ifPresent(currentTab -> {
            log.debug("Current tab found {}.", currentTab.getText());
            currentTab.setOnCloseRequest(this::handleCloseRequest);
            Optional.ofNullable(currentTab.getTabPane())
                .map(TabPane::getSelectionModel)
                .map(SelectionModel::getSelectedItem)
                .ifPresent(parentTab -> parentTab.setOnCloseRequest(this::handleCloseRequest));
        });

        consumeButton.getScene().getWindow().setOnCloseRequest(this::handleCloseRequest);
    }

    private void handleCloseRequest(Event event) {
        log.debug("Close event handler called from {}.", event.getSource());
        kafkaRecordConsumer.getShutdown().set(true);
    }

    private void unsubscribeFromFxElementsEvents() {
        getCurrentTab().ifPresent(currentTab -> {
            currentTab.setOnCloseRequest(null);
            Optional.ofNullable(currentTab.getTabPane())
                .map(TabPane::getSelectionModel)
                .map(SelectionModel::getSelectedItem)
                .ifPresent(parentTab -> parentTab.setOnCloseRequest(null));
        });

        consumeButton.getScene().getWindow().setOnCloseRequest(null);
    }

    private class CustomSelectListener implements ChangeListener<ConsumerRecordSimple> {

        @Override
        public void changed(ObservableValue<? extends ConsumerRecordSimple> observableValue,
            ConsumerRecordSimple oldConsumerRecordSimple,
            ConsumerRecordSimple consumerRecordSimple) {
            if (consumerRecordSimple == null) {
                return;
            }

            String payloadValue = consumerRecordSimple.getValue();

            if (RecordFormatter.isJson(payloadValue)) {
                payloadValue = RecordFormatter.formatJson(payloadValue);
            } else if (RecordFormatter.isXmlValid(payloadValue)) {
                payloadValue = RecordFormatter.formatXml(payloadValue);
            }

            WebEngine engine = webView.getEngine();
            engine.loadContent("<html><head>" +
                "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/default.min.css\">" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js\"></script>" +
                "</head><body><pre id=\"code\">" + payloadValue + "</pre>" +
                "</body></html>");

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    WebEngine webEngine = webView.getEngine();
                    webEngine.executeScript("var result = hljs.highlightAuto(document.getElementById('code').innerText);" +
                        "document.getElementById('code').innerHTML = result.value");
                }
            });
        }
    }
}
