package org.basex.gui;

import java.awt.*;
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
 * @author BaseX Team 2018, BSD License
 * @author Alexander Holupirek
 */
public abstract class GUIMacOS {
  /** Reference to the main UI. */
  final GUI main;

  /**
   * Creates a Java-specific instance of this class.
   * @param main reference to main window
   * @return class instance, or {@code null} if no instance could be created
   */
  static GUIMacOS get(final GUI main) {
    try {
      return Prop.JAVA8 ? new GUIMacOSX(main) : new GUIMacOSX9(main);
    } catch(final Exception ex) {
      Util.errln("Failed to initialize native Mac OS X interface:");
      Util.stack(ex);
      return null;
    }
  }

  /**
   * Constructor.
   * @param main reference to main window
   */
  GUIMacOS(final GUI main) {
    this.main = main;
  }

  /**
   * Determines native full screen support.
   * @return full screen support
   */
  public static boolean nativeFullscreen() {
    if(Prop.JAVA8) {
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
   * Initializes macOS-specific settings.
   */
  public abstract void init();

  /**
   * Moves main menu bar from application window into default menu at the top of the screen.
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
