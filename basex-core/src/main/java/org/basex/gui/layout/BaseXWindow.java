package org.basex.gui.layout;

import java.awt.*;

import org.basex.gui.*;

/**
 * Project-specific window interface.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface BaseXWindow {
  /**
   * Returns the window reference.
   * @return window
   */
  Window component();

  /**
   * Returns the GUI.
   * @return GUI
   */
  GUI gui();

  /**
   * Returns the dialog.
   * @return dialog (can be {@code null})
   */
  BaseXDialog dialog();
}
