/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.kafka.ProduceOneKafkaRecordTask;
import java.util.UUID;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.RecordMetadata;

@Log4j2
@Setter
public class ProducerPane {

  private String clusterName;
  private String topicName;

  @FXML
  private TextArea textArea;

  @FXML
  private TextField textField;

  @FXML
  private Button sendButton;

  @FXML
  public void produce() {
    Task<RecordMetadata> produceOneKafkaRecordTask = new ProduceOneKafkaRecordTask(clusterName, topicName, textField.getText(), textArea.getText());

    produceOneKafkaRecordTask.setOnSucceeded(workerStateEvent -> {
      sendButton.setDisable(false);
      sendButton.setText("Produce Now");
    });

    produceOneKafkaRecordTask.setOnFailed(workerStateEvent -> {
      log.info("task failed", workerStateEvent.getSource().getException());

      sendButton.setDisable(false);
      sendButton.setText("Produce Now");
    });

    Thread loadDataThread = new Thread(produceOneKafkaRecordTask);
    loadDataThread.start();

    sendButton.setDisable(true);
    sendButton.setText("Producing now...");
  }

  @FXML
  private void generateKey() {
    textField.setText(UUID.randomUUID().toString());
  }

  @FXML
  private void generateValue() {
    textArea.setText(UUID.randomUUID().toString());
  }

}
