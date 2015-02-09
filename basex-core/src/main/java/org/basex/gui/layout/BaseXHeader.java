package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;

import javax.swing.border.*;

/**
 * Header label.
 *
 * @author BaseX Team 2005-15, BSD License
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
  }

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    setBorder(new EmptyBorder(-4, 0, -LABEL.getFontMetrics(lfont).getLeading() / 2, 2));
    setFont(lfont);
  }
}
