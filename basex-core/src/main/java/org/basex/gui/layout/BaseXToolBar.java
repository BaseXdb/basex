package org.basex.gui.layout;

import javax.swing.*;

/**
 * Project specific tool bar implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class BaseXToolBar extends JToolBar {
  /**
   * Default constructor.
   */
  public BaseXToolBar() {
    setBorder(BaseXLayout.border(0, 0, 0, 0));
    setFloatable(false);
    setOpaque(false);
  }
}
