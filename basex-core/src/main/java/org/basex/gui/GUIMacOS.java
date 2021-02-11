package org.basex.gui;

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
public abstract class GUIMacOS {
  /** Reference to the main UI. */
  final GUI main;
  /** System property identifier. */
  private static final String P_SCREEN_MENU_BAR = "apple.laf.useScreenMenuBar";

  /**
   * Creates a Java-specific instance of this class.
   * @param main reference to main window
   */
  static void init(final GUI main) {
    try {
      // show menu in the screen menu instead of inside the application window
      System.setProperty(P_SCREEN_MENU_BAR, "true");
      if(Prop.JAVA8) new GUIMacOSX(main);
      else new GUIMacOSX9(main);
    } catch(final Exception ex) {
      Util.errln("Failed to initialize native Mac OS X interface:");
      Util.stack(ex);
    }
  }

  /**
   * Constructor.
   * @param main reference to main window
   */
  GUIMacOS(final GUI main) {
    this.main = main;
  }
}
