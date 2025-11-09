/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AclsSimpleTable {
    private String resourceType;
    private String resourceName;
    private String patternType;
    private String principal;
    private String host;
    private String operation;
    private String permissionType;
}