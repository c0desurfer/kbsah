/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ClusterContentPane implements ContenPane {

  private String clusterName;

}
