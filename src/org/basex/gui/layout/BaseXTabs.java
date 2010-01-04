package org.basex.gui.layout;

import java.awt.Window;
import javax.swing.JTabbedPane;

/**
 * Project specific TabbedPane implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class BaseXTabs extends JTabbedPane {
  /**
   * Default constructor.
   * @param win parent window
   */
  public BaseXTabs(final Window win) {
    super();
    BaseXLayout.addInteraction(this, null, win);
  }
}
