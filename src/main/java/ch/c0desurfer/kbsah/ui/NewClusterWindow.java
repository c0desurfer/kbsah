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
import ch.c0desurfer.kbsah.ui.utils.FormsUtils;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class NewClusterWindow implements Initializable {

  private final Cluster cluster = new Cluster();

  private final Form form = Form.of(
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

  @FXML
  private GridPane gridPane;

  @FXML
  private Button cancelButton;

  @FXML
  public void cancel() {
    log.debug("Canceling kafka cluster creation dialog.");
    ((Stage) cancelButton.getScene().getWindow()).close();
  }

  @FXML
  public void create() {
    form.persist();

    if (!form.isValid()) {
      log.warn("User clicked create in cluster creation dialog but form is not valid yet.");
      return;
    }

    String clusterName = cluster.getClusterName().getValue();

    log.debug("User entered '{}' for new kafka cluster name while adding cluster.", clusterName);

    if (Clusters.builder().build().clusterExists(clusterName)) {
      log.warn("Kafka cluster with name '{}' already exists.", clusterName);

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Kafka Cluster Exists");
      alert.setHeaderText(null);
      alert.setContentText("The kafka cluster with name " + clusterName + " already exists!");

      alert.showAndWait();

      return;
    }

    Clusters.builder().build().addCluster(clusterName, cluster.getBootstrapAddress().getValue(), cluster.getClientProperties().getValue());

    ((Stage) cancelButton.getScene().getWindow()).close();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    FormRenderer fr = new FormRenderer(form);
    FormsUtils.searchAndSetControlsLabelWidth(fr, 20);
    gridPane.add(fr, 0, 0);
  }
}
