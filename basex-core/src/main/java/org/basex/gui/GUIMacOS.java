package org.basex.gui;

import java.awt.*;

import org.basex.util.*;

/**
 * Provides macOS-specific interface options.
 *
 * Differentiates between java runtime versions:
 *
 * - Java 8 runtime uses Apple–specific com.apple.eawt and com.apple.eio packages.
 * - Java 9 or greater runtime leverages new APIs, such as java.awt.Desktop,
 *   which supersede the macOS APIs and are platform-independent.
 *
 * @author BaseX Team 2018, BSD License
 * @author Alexander Holupirek
 */
public abstract class GUIMacOS {

  /** Reference to the main UI. */
  GUI main;

  /**
   * Determines native full screen support.
   * @return full screen support
   */
  public static boolean nativeFullscreen() {
    if (Prop.JAVA8) {
      try {
        Class.forName("com.apple.eawt.FullScreenUtilities");
      } catch(final ClassNotFoundException ex) {
        Util.debug(ex);
        return false;
      }
    }
    return true;
  }

  // ABSTRACT METHODS =============================================================================
  /**
   * Sets a value for the badge in the dock.
   * @param value string value
   * @throws Exception if any error occurs
   */
  public abstract void setBadge(String value) throws Exception;

  /**
   * Initializes this macOS-specific settings.
   * @param gui main UI reference
   * @throws Exception if any error occurs
   */
  public abstract void init(GUI gui);

  /**
   * Moves main menu bar from application window into default menu at the top of the screen.
   *
   * @param gui main BaseX UI
   * @param menu menu to be adjusted
   */
  public abstract void adjustMenuBar(GUI gui, GUIMenu menu);

  /**
   * Enables native full screen.
   * @param window the window
   */
  public abstract void enableOSXFullscreen(Window window);
}
