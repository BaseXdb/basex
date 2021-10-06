package org.basex.gui;

import java.awt.*;
import java.awt.desktop.*;

import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Provides macOS-specific interface options.
 * Differentiates between java runtime versions:
 * <ul>
 *   <li> Java 8 runtime uses Apple–specific com.apple.eawt and com.apple.eio packages.</li>
 *   <li> Java 9 or greater runtime leverages new APIs, such as java.awt.Desktop,
 *     which supersede the macOS APIs and are platform-independent.</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Alexander Holupirek
 */
public final class GUIMacOS implements AboutHandler, PreferencesHandler {
  /** Reference to the main UI. */
  final GUI main;

  /**
   * Creates a Java-specific instance of this class.
   * @param main reference to main window
   */
  static void init(final GUI main) {
    try {
      // show menu in the screen menu instead of inside the application window
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      new GUIMacOS(main);
    } catch(final Exception ex) {
      Util.errln("Failed to initialize native Mac OS X interface:");
      Util.stack(ex);
    }
  }

  /**
   * Constructor.
   * @param main reference to main window
   * @throws Exception if any error occurs
   */
  GUIMacOS(final GUI main) throws Exception {
    this.main = main;

    final Class<?> tbClass = Class.forName("java.awt.Taskbar");
    final Object tb = tbClass.getMethod("getTaskbar").invoke(null);
    tbClass.getMethod("setIconImage", Image.class).invoke(tb, BaseXImages.get("logo_256"));
  }

  /**
   * Shows the about dialog.
   * @param e about event received
   */
  @Override
  public void handleAbout(final AboutEvent e) {
    DialogAbout.show(main);
  }

  /**
   * Shows the preferences dialog.
   * @param e preference event received
   */
  @Override
  public void handlePreferences(final PreferencesEvent e) {
    DialogAbout.show(main);
  }
}
