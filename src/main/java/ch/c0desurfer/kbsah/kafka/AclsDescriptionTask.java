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
import ch.c0desurfer.kbsah.ui.model.AclsSimpleTable;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;

@Log4j2
@Setter
@AllArgsConstructor
public class AclsDescriptionTask extends Task<ObservableList<AclsSimpleTable>> {

    String clusterName;

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
    protected ObservableList<AclsSimpleTable> call() throws Exception {
        try (AdminClient adminClient = AdminClient.create(Clusters.builder().build().getAllKafkaAdminClientProperties(clusterName))) {

            DescribeAclsResult describeAclsResult = adminClient.describeAcls(AclBindingFilter.ANY);

            KafkaFuture<Collection<AclBinding>> values = describeAclsResult.values();
            Collection<AclBinding> aclBindings = values.get(kafkaClientTimeout.getValue(), TimeUnit.SECONDS);

            List<AclsSimpleTable> aclsSimpleTableList = aclBindings.stream().map(aclBinding -> new AclsSimpleTable(
                aclBinding.pattern().resourceType().toString(),
                aclBinding.pattern().name(),
                aclBinding.pattern().patternType().toString(),
                aclBinding.entry().principal(),
                aclBinding.entry().host(),
                aclBinding.entry().operation().toString(),
                aclBinding.entry().permissionType().toString())).collect(Collectors.toList());

            return FXCollections.observableArrayList(aclsSimpleTableList);
        }
    }

}
