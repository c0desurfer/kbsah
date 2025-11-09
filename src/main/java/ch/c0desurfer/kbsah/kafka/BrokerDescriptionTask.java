/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.kafka;

import ch.c0desurfer.kbsah.settings.Clusters;
import ch.c0desurfer.kbsah.ui.model.BrokerSimpleTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Log4j2
@Setter
@Getter
@AllArgsConstructor
public class BrokerDescriptionTask extends Task<ObservableList<BrokerSimpleTable>> {

    private String clusterName;
    private String brokerId;

    @Override
    protected ObservableList<BrokerSimpleTable> call() throws InterruptedException, ExecutionException, TimeoutException {
        try (AdminClient adminClient = AdminClient.create(Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName))) {

            ConfigResource cr = new ConfigResource(ConfigResource.Type.BROKER, brokerId);
            DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singleton(cr));
            Map.Entry<ConfigResource, KafkaFuture<Config>> next = describeConfigsResult.values().entrySet().iterator().next();

            Config config = next.getValue().get(30, TimeUnit.SECONDS);

            List<BrokerSimpleTable> brokerSimpleTableList = config.entries().stream().map(configEntry ->
                    new BrokerSimpleTable(configEntry.name(),
                        configEntry.value(),
                        configEntry.source().toString(),
                        configEntry.isDefault(),
                        configEntry.isReadOnly(),
                        configEntry.isSensitive()))
                .collect(Collectors.toList());

            return FXCollections.observableArrayList(brokerSimpleTableList);
        }
    }

}
