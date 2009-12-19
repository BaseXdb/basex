package org.basex.gui.layout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * Project specific Label implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXLabel extends JLabel {
  /** Icon type. */
  public enum Icon {
    /** Warning icon. */ WARN,
    /** Error icon. */ ERR,
    /** Success icon. */ OK
  };
  
  /**
   * Constructor.
   * @param txt label text
   */
  public BaseXLabel(final String txt) {
    this(txt, false, false);
  }

  /**
   * Constructor.
   * @param txt label text
   * @param dist distance to next component
   * @param bold bold flag
   */
  public BaseXLabel(final String txt, final boolean dist, final boolean bold) {
    super(txt);
    if(dist) setBorder(0, 0, 8, 0);
    setFont(getFont().deriveFont(bold ? 1 : 0));
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   */
  public void setBorder(final int t, final int l, final int b, final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
  }

  /**
   * Shows an text, preceded by a state icon.
   * If the text is <code>null</code>, no text and icon is shown.
   * @param text warning text
   * @param icon flag for displaying a warning or error icon
   */
  public void setText(final String text, final Icon icon) {
    ImageIcon ic = null;
    switch(icon) {
      case WARN: ic = BaseXLayout.icon("warn");  break;
      case ERR : ic = BaseXLayout.icon("error"); break;
      case OK  : ic = BaseXLayout.icon("ok");    break;
    }
    setIcon(text == null ? null : ic);
    setText(text == null ? " " : text);
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
