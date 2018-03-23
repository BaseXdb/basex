package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;

import javax.swing.border.*;

/**
 * Header label.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class BaseXHeader extends BaseXLabel {
  /**
   * Constructor.
   * @param string string
   */
  public BaseXHeader(final String string) {
    super(string, true, false);
    setForeground(dgray);
    setBorder(new EmptyBorder(-4, 0, -getFontMetrics(lfont).getLeading() / 2, 2));
    setFont(lfont);
  }
}
