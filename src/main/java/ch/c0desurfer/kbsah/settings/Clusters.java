/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.settings;

import ch.c0desurfer.kbsah.Main;
import ch.c0desurfer.kbsah.ui.model.Cluster;
import javafx.beans.property.SimpleStringProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;


@Log4j2
@Getter
@Builder
public class Clusters {

  private final Preferences preferences = Preferences.userNodeForPackage(Main.class).node("clusters");

  public void addCluster(String clusterName, String bootstrapAddress, String clientProperties) {
    log.debug("Adding cluster '{}' to preferences.", clusterName);

    preferences.node(clusterName).put("bootstrapAddress", bootstrapAddress);
    preferences.node(clusterName).put("clientProperties", clientProperties);

    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      log.error("Flushing preferences after adding cluster failed.", e);
    }
  }

  public boolean clusterExists(String clusterName) {
    try {
      return preferences.nodeExists(clusterName);
    } catch (BackingStoreException e) {
      log.error("Checking cluster existence in preferences failed.", e);
    }
    return false;
  }

  public List<Cluster> getClusters() {
    List<Cluster> clusters = new ArrayList<>();
    try {
      clusters = Arrays.stream(preferences.childrenNames()).map(clusterName -> {
        String bootstrapAddress = preferences.node(clusterName).get("bootstrapAddress", "");
        String clientProperties = preferences.node(clusterName).get("clientProperties", "");
        return new Cluster(
            new SimpleStringProperty(clusterName),
            new SimpleStringProperty(bootstrapAddress),
            new SimpleStringProperty(clientProperties));
      }).collect(Collectors.toList());
    } catch (BackingStoreException e) {
      log.error("Could not retrieve cluster names from preferences.");
    }
    return clusters;
  }

  public void setClusterAutoConnect(String clusterName, boolean autoConnect) {
    preferences.node(clusterName).putBoolean("autoConnect", autoConnect);
  }

  public boolean getClusterAutoConnect(String clusterName) {
    return preferences.node(clusterName).getBoolean("autoConnect", false);
  }

  public Properties getAllKafkaAdminClientProperties(String clusterName) {
    Properties adminClientProperties = new Properties();
    adminClientProperties.put(BOOTSTRAP_SERVERS_CONFIG, preferences.node(clusterName).get("bootstrapAddress", ""));

    String clientProperties = getClientProperties(clusterName);
    if (!clientProperties.isBlank()) {
      Properties properties = new Properties();
      clientProperties.lines().forEach(clusterProperty -> {
        try {
          properties.load(new StringReader(String.valueOf(clusterProperty)));
        } catch (IOException e) {
          log.error("Could not read string while reading client properties.", e);
        }
        adminClientProperties.putAll(properties);
      });
    }

    return adminClientProperties;
  }

  public String getClientProperties(String clusterName) {
    return preferences.node(clusterName).get("clientProperties", "");
  }

  public String getBootStrapAddress(String clusterName) {
    return preferences.node(clusterName).get("bootstrapAddress", "");
  }

  public void deleteCluster(String clusterName) {
    try {
      preferences.node(clusterName).removeNode();
    } catch (BackingStoreException e) {
      log.error("Could not remove cluster {}.", clusterName, e);
    }
  }
}
