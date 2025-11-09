/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import ch.c0desurfer.kbsah.settings.Clusters;
import ch.c0desurfer.kbsah.ui.model.Cluster;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.prefs.BackingStoreException;

@Log4j2
@Setter
public class EditClusterWindow {

  private Cluster cluster;

  private String clusterName;
  private String originalClusterName;

  private Form form;

  @FXML
  private Button cancelButton;

  @FXML
  private GridPane gridPane;

  public void createForm() {
    cluster = new Cluster(
        new SimpleStringProperty(originalClusterName),
        new SimpleStringProperty(Clusters.builder().build().getBootStrapAddress(originalClusterName)),
        new SimpleStringProperty(Clusters.builder().build().getClientProperties(originalClusterName)));

    form = Form.of(
        Group.of(
            Field.ofStringType(cluster.getClusterName())
                .label("Cluster Name")
                .required("Cluster name is required.")
                .validate(StringLengthValidator
                    .atLeast(2, "Cluster name is too short.")),
            Field.ofStringType(cluster.getBootstrapAddress())
                .label("Bootstrap Address")
                .required("Bootstrap address is required.")
                .validate(StringLengthValidator
                    .atLeast(6, "Bootstrap address is too short.")),
            Field.ofStringType(cluster.getClientProperties())
                .label("Client Properties")
                .multiline(true)
        )
    );

    FormRenderer fr = new FormRenderer(form);
    gridPane.add(fr, 0, 0);
  }

  @FXML
  private void save() {
    form.persist();

    if (!form.isValid()) {
      log.warn("User clicked create in cluster edit dialog but form is not valid yet.");
      return;
    }

    log.debug("User entered '{}' for new kafka cluster name while editing cluster.", clusterName);

    boolean autoConnect = Clusters.builder().build().getClusterAutoConnect(originalClusterName);

    if (!originalClusterName.equals(cluster.getClusterName().getValue())) {
      try {
        Clusters.builder().build().getPreferences().node(originalClusterName).removeNode();
      } catch (BackingStoreException e) {
        log.error("Could not delete node {} for renaming.", originalClusterName, e);
      }
    }

    Clusters.builder().build().addCluster(
        cluster.getClusterName().getValue(),
        cluster.getBootstrapAddress().getValue(),
        cluster.getClientProperties().getValue());

    if (autoConnect) {
      Clusters.builder().build().setClusterAutoConnect(cluster.getClusterName().getValue(), true);
    }

    ((Stage) cancelButton.getScene().getWindow()).close();
  }

  @FXML
  private void cancel() {
    log.debug("Canceling kafka cluster creation dialog.");
    ((Stage) cancelButton.getScene().getWindow()).close();
  }
}
