package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.GUIConstants.Msg;

/**
 * Project specific Label implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class BaseXLabel extends JLabel {
  /**
   * Constructor.
   */
  public BaseXLabel() {
    this("", false, false);
  }

  /**
   * Constructor, specifying a label text.
   * @param text label text
   */
  public BaseXLabel(final String text) {
    this(text, false, false);
  }

  /**
   * Constructor, specifying a label text, vertical distance to the next
   * component, and a property for printing the label in bold.
   * @param text label text
   * @param dist vertical distance to next component
   * @param bold bold flag
   */
  public BaseXLabel(final String text, final boolean dist, final boolean bold) {
    super(text);
    if(dist) border(0, 0, getFont().getSize() / 2, 0);
    if(bold) BaseXLayout.boldFont(this);
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public final BaseXLabel border(final int t, final int l, final int b, final int r) {
    setBorder(BaseXLayout.border(t, l, b, r));
    return this;
  }

  /**
   * Sets the text color.
   * @param c color
   * @return self reference
   */
  public final BaseXLabel color(final Color c) {
    setForeground(c);
    return this;
  }

  /**
   * Resizes the used font.
   * @param factor resize factor
   * @return self reference
   */
  public final BaseXLabel resize(final float factor) {
    BaseXLayout.resizeFont(this, factor);
    return this;
  }

  /**
   * Shows an text, preceded by a state icon.
   * If the text is {@code null}, no text and icon is shown.
   * @param text warning text
   * @param icon flag for displaying a warning or error icon
   * @return self reference
   */
  public final BaseXLabel setText(final String text, final Msg icon) {
    setIcon(text == null ? null : icon.small);
    setText(text == null ? " " : text);
    return this;
  }

  /**
   * Shows an text. If required, chops the last characters.
   * @param text warning text
   * @param width maximum width
   * @return self reference
   */
  public final BaseXLabel setChoppedText(final String text, final int width) {
    final FontMetrics fm = getFontMetrics(getFont());
    String txt = text;
    if(width < fm.stringWidth(txt)) {
      int tl = txt.length();
      while(tl > 0 && width < fm.stringWidth(txt + Text.DOTS)) txt = txt.substring(0, --tl);
      txt += Text.DOTS;
    }
    super.setText(txt);
    return this;
  }

  /**
   * Chooses a large font.
   * @return self reference
   */
  public final BaseXLabel large() {
    final Font f = getFont();
    setFont(new Font(f.getName(), Font.BOLD, (int) (f.getSize2D() * 1.4)));
    return this;
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
