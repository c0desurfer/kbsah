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
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.Node;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Log4j2
@Setter
@AllArgsConstructor
public class ClusterNodesFetchTask extends Task<Collection<Node>> {

    private String clusterName;

    @Override
    protected Collection<Node> call() throws InterruptedException, ExecutionException, TimeoutException {
        try (AdminClient adminClient = AdminClient.create(Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName))) {
            return adminClient.describeCluster().nodes().get(30, TimeUnit.SECONDS);
        }
    }
}
