package org.basex.gui.layout;

import java.awt.Window;
import javax.swing.JTabbedPane;

/**
 * Project specific TabbedPane implementation.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final Window win) {
    super();
    BaseXLayout.addInteraction(this, win);
  }
}
