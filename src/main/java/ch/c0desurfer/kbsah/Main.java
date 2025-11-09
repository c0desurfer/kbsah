/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@Log4j2
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/MainWindow.fxml")));

        Scene scene = new Scene(parent);

        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setTitle("Kafka Browsah");
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
