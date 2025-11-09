/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicListViewItem {
  @FXML
  private Label topicNameLabel;

  @FXML
  private Label topicInfoLabel;
}
