/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogUtils {
  public static void displayDialog(Parent parent, String title) {
    Stage stage = new Stage();
    stage.setTitle(title);
    stage.setScene(new Scene(parent));
    stage.setResizable(false);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.show();
  }

}
