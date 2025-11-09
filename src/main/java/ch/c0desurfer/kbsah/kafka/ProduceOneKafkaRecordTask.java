/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.kafka;

import ch.c0desurfer.kbsah.Main;
import ch.c0desurfer.kbsah.settings.Clusters;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
@Setter
@AllArgsConstructor
public class ProduceOneKafkaRecordTask extends Task<RecordMetadata> {

  private String clusterName;
  private String topicName;

  private String key;
  private String value;

  private final IntegerProperty kafkaClientTimeout = new SimpleIntegerProperty(30);

  private final PreferencesFx preferencesFx = PreferencesFx.of(Main.class,
      Category.of("Kafka",
          Group.of("Client",
              Setting.of("Timeout", kafkaClientTimeout)
          ))
  );

  @Override
  protected RecordMetadata call() throws InterruptedException, TimeoutException, ExecutionException {

    String CLIENT_ID = "KAFKABROWSAH";

    Properties clientProperties = Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName);

    clientProperties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
    clientProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    clientProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    return Executors.newSingleThreadExecutor().submit(() -> {
      try (KafkaProducer<String, String> producer = new KafkaProducer<>(clientProperties)) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, value);
        return producer.send(producerRecord).get(kafkaClientTimeout.getValue(), TimeUnit.SECONDS);
      }
    }).get(15, TimeUnit.SECONDS);
  }
}
