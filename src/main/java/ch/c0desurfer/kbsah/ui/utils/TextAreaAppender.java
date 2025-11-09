/*
 * Copyright (C) 2025. c0desurfer. All rights reserved.
 *
 * This file is part of the kafkabrowsah project. Copying, (re-)using or
 * (re-)distributing in whole or in parts, without prior written permission,
 * is strictly prohibited.
 *
 */

package ch.c0desurfer.kbsah.ui.utils;


import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.Setter;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(name = "TextAreaAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class TextAreaAppender extends AbstractAppender {

  @Setter
  private static TextArea textArea;

  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final Lock readLock = rwLock.readLock();

  protected TextAreaAppender(String name, Filter filter,
      Layout<? extends Serializable> layout, boolean ignoreExceptions,
      Property[] properties) {
    super(name, filter, layout, ignoreExceptions, properties);
  }

  @PluginFactory
  @SuppressWarnings("unused") // class is indirectly constructed by log4j2
  public static TextAreaAppender createAppender(
      @PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginElement("Properties") Property[] properties) {
    if (name == null) {
      LOGGER.error("No name provided for TextAreaAppender.");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new TextAreaAppender(name, filter, layout, true, properties);
  }

  @Override
  public void append(LogEvent event) {
    readLock.lock();

    final String message = new String(getLayout().toByteArray(event));

    try {
      Platform.runLater(() -> {
        try {
          if (textArea != null) {
            if (textArea.getText().isEmpty()) {
              textArea.setText(message);
            } else {
              textArea.selectEnd();
              textArea.insertText(textArea.getText().length(), message);
            }
          }
        } catch (final Throwable t) {
          System.out.println("Error while appending to TextArea " + t.getMessage() + ".");
        }
      });
    } catch (final IllegalStateException ex) {
      ex.printStackTrace();
    } finally {
      readLock.unlock();
    }
  }
}