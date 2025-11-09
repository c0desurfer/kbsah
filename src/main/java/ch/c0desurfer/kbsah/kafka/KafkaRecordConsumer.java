package ch.c0desurfer.kbsah.kafka;

import ch.c0desurfer.kbsah.Main;
import ch.c0desurfer.kbsah.settings.Clusters;
import ch.c0desurfer.kbsah.ui.model.ConsumerRecordSimple;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KafkaRecordConsumer extends Thread {

    @Getter
    private final ObservableList<ConsumerRecordSimple> records = FXCollections.observableArrayList();

    @Getter
    private final BooleanProperty shutdown = new SimpleBooleanProperty(false);

    private final KafkaConsumer<String, String> consumer;

    private final int limit;

    private static final StringProperty kafkaClientConsumerGroupId = new SimpleStringProperty("KAFKABROWSAH");
    private static final StringProperty kafkaClientId = new SimpleStringProperty("KAFKABROWSAH");

    static {
        PreferencesFx.of(Main.class,
                Category.of("Kafka",
                        Group.of("Client",
                                Setting.of("Consumer Group ID", kafkaClientConsumerGroupId),
                                Setting.of("Client ID", kafkaClientId)
                        ))
        );
    }

    public KafkaRecordConsumer(String clusterName, String topicName, String startPointChoice, int limit) {

        this.limit = limit;

        Properties clientProperties = Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName);

        clientProperties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaClientConsumerGroupId.getValue());
        clientProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaClientId.getValue());
        clientProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        clientProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        clientProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        consumer = new KafkaConsumer<>(clientProperties);

        consumer.subscribe(Collections.singleton(topicName), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // just overriding
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                log.debug("Partitions assigned @ {}.", topicName);

                switch (startPointChoice) {
                    case "Now" -> {
                        log.debug("Setting offset for now.");
                        seekToDate(partitions, ZonedDateTime.now(ZoneId.systemDefault()));
                    }
                    case "Last Hour" -> {
                        log.debug("Setting offset for last hour.");
                        seekToDate(partitions, ZonedDateTime.now(ZoneId.systemDefault()).minusHours(1));
                    }
                    case "Today" -> {
                        log.debug("Setting offset for today.");
                        seekToDate(partitions, ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()));
                    }
                    case "Yesterday" -> {
                        log.debug("Setting offset for yesterday.");
                        seekToDate(partitions, ZonedDateTime.now(ZoneId.systemDefault()).minusDays(1).toLocalDate().atStartOfDay(ZoneId.systemDefault()));
                    }
                    case "From Beginning" -> {
                        log.debug("Seeking to beginning.");
                        consumer.seekToBeginning(consumer.assignment());
                    }
                    default -> log.debug("Nothing to do after partition assignment.");
                }

            }
        });
    }

    private void seekToDate(Collection<TopicPartition> partitions, ZonedDateTime zonedDateTime) {
        log.debug("Seeking to date and time {}.", zonedDateTime.toLocalDateTime());

        Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());

        Map<TopicPartition, Long> topicPartitionToTimestampMap = partitions.stream()
                .collect(Collectors.toMap(tp -> tp, tp -> timestamp.getTime()));
        Map<TopicPartition, OffsetAndTimestamp> topicPartitionOffsetAndTimestampMap = consumer.offsetsForTimes(topicPartitionToTimestampMap);

        topicPartitionOffsetAndTimestampMap.entrySet().stream()
                .filter(topicPartitionOffsetAndTimestampEntry -> topicPartitionOffsetAndTimestampEntry.getValue() != null).collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((topicPartition, offsetAndTimestamp) -> consumer.seek(topicPartition, offsetAndTimestamp.offset()));
    }

    @Override
    public void run() {
        while (!shutdown.getValue()) {

            if (limit > 0 && records.size() >= limit) {
                shutdown();
                continue;
            }

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(500));

            log.debug("Received {} records ({}).", consumerRecords.count(), consumer.assignment().stream()
                .findFirst()
                .map(TopicPartition::topic)
                .orElse("unknown"));

            consumerRecords.forEach(consumerRecord -> {

                if (limit > 0 && records.size() >= limit) {
                    shutdown();
                    return;
                }

                records.add(new ConsumerRecordSimple(consumerRecord.key(), consumerRecord.offset(), consumerRecord.partition(), new Date(consumerRecord.timestamp()),
                        consumerRecord.value()));
            });
        }

        log.debug("Closing consumer ({}).", consumer.assignment().stream()
            .findFirst()
            .map(TopicPartition::topic)
            .orElse("unknown"));
        consumer.close();
    }

    private void shutdown() {
        log.debug("Limit reached at {}.", limit);
        shutdown.set(true);
    }
}
