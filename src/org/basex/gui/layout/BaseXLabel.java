package org.basex.gui.layout;

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
    this(label, false, false);
  }

  /**
   * Default Constructor.
   * @param txt label text
   * @param dist distance to next component
   * @param bold bold flag
   */
  public BaseXLabel(final String txt, final boolean dist, final boolean bold) {
    super(txt);
    if(dist) setBorder(0, 0, 8, 0);
    final int s = bold ? 1 : 0;
    if(getFont().getStyle() != s) setFont(getFont().deriveFont(s));
  }

  /**
   * Default Constructor.
   * @param txt label text
   * @param dist add some distance below
   */
  public BaseXLabel(final String txt, final boolean dist) {
    this(txt, dist, false);
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
