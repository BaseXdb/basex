package org.basex.gui.layout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * Project specific Label implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXLabel extends JLabel {
  /**
   * Default Constructor.
   * @param label button title
   */
  public BaseXLabel(final String label) {
    super(label);
  }

  /**
   * Default Constructor.
   * @param icon icon
   */
  public BaseXLabel(final ImageIcon icon) {
    super(icon);
  }

  /**
   * Default Constructor.
   * @param label button title
   * @param dist distance to next component
   */
  public BaseXLabel(final String label, final int dist) {
    super(label);
    setBorder(0, 0, dist, 0);
  }

  /**
   * Default Constructor.
   * @param label button title
   * @param dist add some distance below
   */
  public BaseXLabel(final String label, final boolean dist) {
    this(label, dist ? 10 : 0);
    if(dist) setFont(getFont().deriveFont(1));
  }

  /**
   * Set label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   */
  public void setBorder(final int t, final int l, final int b, final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
  }
}
