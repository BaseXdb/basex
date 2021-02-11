package org.basex.gui.layout;

import javax.swing.border.*;

import org.basex.gui.*;

/**
 * Header label.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXHeader extends BaseXLabel {
  /**
   * Constructor.
   * @param string string
   */
  public BaseXHeader(final String string) {
    super(string, true, false);
    setForeground(GUIConstants.dgray);
    resize(1.7f);
    setBorder(new EmptyBorder(-2, 0, 8, 2));
  }
}
