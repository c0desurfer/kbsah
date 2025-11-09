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
import ch.c0desurfer.kbsah.ui.model.TopicSimple;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Log4j2
@Setter
@AllArgsConstructor
public class TopicDescriptionTask extends Task<ObservableList<TopicSimple>> {

    private String clusterName;

    private final static IntegerProperty kafkaClientTimeout = new SimpleIntegerProperty(30);

    static {
        PreferencesFx.of(Main.class,
                Category.of("Kafka",
                        Group.of("Client",
                                Setting.of("Timeout", kafkaClientTimeout)
                        ))
        );
    }

    @Override
    protected ObservableList<TopicSimple> call() throws InterruptedException, ExecutionException, TimeoutException {

        try (AdminClient adminClient = AdminClient.create(Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName))) {

            ObservableList<TopicSimple> observableList = FXCollections.observableArrayList();
            Collection<TopicListing> topicListings = adminClient.listTopics().listings().get(kafkaClientTimeout.getValue(), TimeUnit.SECONDS);

            DescribeTopicsResult describeTopicsResult = adminClient
                    .describeTopics(topicListings.stream().map(TopicListing::name).collect(Collectors.toList()));
            Map<String, KafkaFuture<TopicDescription>> values = describeTopicsResult.topicNameValues();

            Objects.requireNonNull(topicListings).forEach(topicListing -> {

                try {
                    TopicDescription topicDescription = values.get(topicListing.name()).get(kafkaClientTimeout.getValue(), TimeUnit.SECONDS);
                    observableList
                            .add(new TopicSimple(topicListing.name(), topicDescription.partitions().size(), topicDescription.partitions().getFirst().replicas().size()));
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    log.error("Could not get topic description for {} in time.", topicListing.name(), e);
                }

            });

            observableList.sort(Comparator.comparing(o -> String.valueOf(o.getTopicName())));

            return observableList;

        }
    }
}
