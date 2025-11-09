module kafkabrowsah {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    requires java.prefs;

    requires com.google.gson;
    requires com.fasterxml.jackson.core;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires static lombok;
    requires java.sql;
    requires kafka.clients;
    requires com.dlsc.formsfx;
    requires com.dlsc.preferencesfx;
    requires org.json;
    requires org.apache.commons.lang3;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.apache.commons.text;

    opens ch.c0desurfer.kbsah;
    opens ch.c0desurfer.kbsah.ui;
    opens ch.c0desurfer.kbsah.ui.model;
    opens ch.c0desurfer.kbsah.kafka;
    opens ch.c0desurfer.kbsah.ui.utils;
}