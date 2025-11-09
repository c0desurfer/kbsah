/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.TopicDescriptionTask;
import ch.c0desurfer.kbsah.kafka.TopicDetailedConfigTask;
import ch.c0desurfer.kbsah.ui.model.TopicConfig;
import ch.c0desurfer.kbsah.ui.model.TopicSimple;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;

@Log4j2
public class TopicsPane extends ClusterContentPane implements Initializable {

  private ObservableList<TopicSimple> filteredItems = FXCollections.observableArrayList();
  private final ObservableList<TopicSimple> unfilteredItems = FXCollections.observableArrayList();

  @FXML
  private TitledPane topicTitledPane;
  @FXML
  private TextField nameFilter;

  @FXML
  private TableView<TopicConfig> detailedTableView;

  @FXML
  private TableView<TopicSimple> simpleTableView;

  @FXML
  private Tab consumerTab;

  @FXML
  private Tab producerTab;

  private ConsumerPane consumerPaneController;

  @Override
  public void showContent() {
    Task<ObservableList<TopicSimple>> fetchTopicsTask = new TopicDescriptionTask(getClusterName());

    fetchTopicsTask.setOnSucceeded(e -> {
      filteredItems = fetchTopicsTask.getValue();
      unfilteredItems.addAll(filteredItems);

      if (!filteredItems.isEmpty()) {
        simpleTableView.setItems(filteredItems);
        simpleTableView.setPlaceholder(new Label("No topics to show."));
        simpleTableView.getSelectionModel().select(0);
      }
    });

    fetchTopicsTask.setOnFailed(workerStateEvent -> {
      log.warn("Could not list topics.", workerStateEvent.getSource().getException());
      simpleTableView.setPlaceholder(new Label(workerStateEvent.getSource().getException().toString()));
    });

    ProgressIndicator progressIndicator = new ProgressIndicator();
    simpleTableView.setPlaceholder(progressIndicator);

    Thread loadDataThread = new Thread(fetchTopicsTask);
    loadDataThread.start();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    nameFilter.textProperty().addListener((observableValue, s, t1) -> updateFilteredData());

    initializeSimpleTable();
    initializeDetailedTable();
  }

  private void initializeDetailedTable() {
    TableColumn<TopicConfig, String> nameTableColumn = new TableColumn<>("Name");
    TableColumn<TopicConfig, String> valueTableColumn = new TableColumn<>("Value");
    TableColumn<TopicConfig, String> sourceTableColumn = new TableColumn<>("Source");
    TableColumn<TopicConfig, String> isSensitiveTableColumn = new TableColumn<>("Sensitive");
    TableColumn<TopicConfig, String> isReadOnlyTableColumn = new TableColumn<>("Read Only");
    TableColumn<TopicConfig, String> synonymsTableColumn = new TableColumn<>("Synonyms");

    nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    valueTableColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    sourceTableColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
    isSensitiveTableColumn.setCellValueFactory(new PropertyValueFactory<>("sensitive"));
    isReadOnlyTableColumn.setCellValueFactory(new PropertyValueFactory<>("readOnly"));
    synonymsTableColumn.setCellValueFactory(new PropertyValueFactory<>("synonyms"));

    detailedTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    detailedTableView.getColumns()
            .addAll(Arrays
                    .asList(nameTableColumn, valueTableColumn, sourceTableColumn, isSensitiveTableColumn, isReadOnlyTableColumn, synonymsTableColumn));
    detailedTableView.setPlaceholder(new Label("No details to show."));
  }

  private void initializeSimpleTable() {
    TableColumn<TopicSimple, TopicSimple> topicTableColumn = new TableColumn<>("Topic");
    topicTableColumn.setCellFactory(topicSimpleTopicSimpleTableColumn -> new CustomTableCell());

    topicTableColumn
        .setCellValueFactory(topicSimpleTopicSimpleCellDataFeatures -> new SimpleObjectProperty<>(topicSimpleTopicSimpleCellDataFeatures.getValue()));
    topicTableColumn.setComparator(Comparator.comparing(o -> String.valueOf(o.getTopicName())));

    simpleTableView.getSelectionModel().selectedItemProperty().addListener(new CustomSelectListener());
    simpleTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    simpleTableView.getColumns().add(topicTableColumn);
  }

  private void updateFilteredData() {
    filteredItems.clear();

    filteredItems.addAll(unfilteredItems.stream()
            .filter(topicSimple -> topicSimple.getTopicName()
                    .contains(nameFilter.getText())).toList());
  }

  private static class CustomTableCell extends TableCell<TopicSimple, TopicSimple> {
    private final TopicListViewItem topicListViewItem;
    private AnchorPane anchorPane;

    public CustomTableCell() {

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/TopicListViewItem.fxml"));
      try {
        anchorPane = loader.load();
      } catch (IOException e) {
        log.error("Loading topic view item failed.", e);
      }
      topicListViewItem = loader.getController();
    }

    @Override
    protected void updateItem(TopicSimple topic, boolean empty) {
      super.updateItem(topic, empty);

      if (topic != null && !empty) {
        topicListViewItem.getTopicNameLabel().setText(topic.getTopicName());
        topicListViewItem.getTopicInfoLabel().setText(
            topic.getPartitions() + " partition(s), " + topic.getReplicas() + " replica(s)");
        setGraphic(anchorPane);
      } else {
        setGraphic(null);
      }
    }
  }

  private class CustomSelectListener implements ChangeListener<TopicSimple> {

    @Override
    public void changed(ObservableValue<? extends TopicSimple> observableValue, TopicSimple oldTopic, TopicSimple topic) {

      if (topic == null) {
        topicTitledPane.setText("Topic");
        return;
      }

      topicTitledPane.setText(topic.getTopicName());

      Task<ObservableList<TopicConfig>> fetchTopicsDetailsTask = new TopicDetailedConfigTask(getClusterName(), topic.getTopicName());

      detailedTableView.getItems().clear();

      fetchTopicsDetailsTask.setOnSucceeded(workerStateEvent -> detailedTableView.setItems(fetchTopicsDetailsTask.getValue()));

      fetchTopicsDetailsTask.setOnFailed(workerStateEvent -> {
        log.warn("Could not fetch topic details.", workerStateEvent.getSource().getException());
        detailedTableView.setPlaceholder(new Label(workerStateEvent.getSource().getException().toString()));
      });

      ProgressIndicator progressIndicator = new ProgressIndicator();
      detailedTableView.setPlaceholder(progressIndicator);

      Thread loadDataThread = new Thread(fetchTopicsDetailsTask);
      loadDataThread.start();

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConsumerPane.fxml"));
      try {
        AnchorPane consumerPane = loader.load();

        if (consumerPaneController != null) {
          consumerPaneController.unsubscribeAndShutdown();
        }

        consumerPaneController = loader.getController();
        
        consumerPaneController.setClusterName(getClusterName());
        consumerPaneController.setTopicName(topic.getTopicName());

        consumerTab.setContent(consumerPane);

      } catch (IOException e) {
        log.error("Loading consumer pane failed.", e);
      }

      FXMLLoader loaderProducer = new FXMLLoader(getClass().getResource("/ProducerPane.fxml"));
      try {
        AnchorPane producerPane = loaderProducer.load();

        ProducerPane producerPaneController = loaderProducer.getController();
        producerPaneController.setClusterName(getClusterName());
        producerPaneController.setTopicName(topic.getTopicName());

        producerTab.setContent(producerPane);

      } catch (IOException e) {
        log.error("Loading producer pane failed.", e);
      }

    }
  }

}
