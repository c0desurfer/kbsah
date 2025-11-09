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
import ch.c0desurfer.kbsah.ui.model.TopicConfig;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

@Log4j2
@Setter
@AllArgsConstructor
public class TopicDetailedConfigTask extends Task<ObservableList<TopicConfig>> {

    private String clusterName;
    private String topicName;

    private static final IntegerProperty kafkaClientTimeout = new SimpleIntegerProperty(30);

    static {
        PreferencesFx.of(Main.class,
            Category.of("Kafka",
                Group.of("Client",
                    Setting.of("Timeout", kafkaClientTimeout)
                ))
        );
    }

    @Override
    protected ObservableList<TopicConfig> call() throws InterruptedException, ExecutionException, TimeoutException {
        try (AdminClient adminClient = AdminClient.create(Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName))) {

            final ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

            Map<ConfigResource, Config> configResourceConfigMap = adminClient
                .describeConfigs(Collections.singleton(configResource)).all().get(kafkaClientTimeout.getValue(), TimeUnit.SECONDS);

            List<TopicConfig> topicConfigList = Objects.requireNonNull(configResourceConfigMap).get(configResource).entries().stream()
                .map(configEntry -> new TopicConfig(configEntry.name(),
                    configEntry.value(),
                    configEntry.source().name(),
                    configEntry.isSensitive(),
                    configEntry.isReadOnly(),
                    configEntry.synonyms().stream()
                        .map(ConfigEntry.ConfigSynonym::name)
                        .collect(Collectors.toList()))).sorted(Comparator.comparing(o -> String.valueOf(o.getName())))
                .collect(Collectors.toList());

            return FXCollections.observableArrayList(topicConfigList);
        }
    }

}
