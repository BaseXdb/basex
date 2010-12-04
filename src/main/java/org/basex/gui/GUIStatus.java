package org.basex.gui;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXMem;
import org.basex.gui.layout.BaseXPanel;

/**
 * This is the status bar of the main window. It displays progress information
 * and includes a memory status.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class GUIStatus extends BaseXPanel {
  /** Memory usage. */
  private final BaseXMem mem;
  /** Status text. */
  private final BaseXLabel label;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  GUIStatus(final AGUI main) {
    super(main);
    BaseXLayout.setHeight(this, getFont().getSize() + 6);
    addMouseListener(this);
    addMouseMotionListener(this);

    layout(new BorderLayout());
    label = new BaseXLabel(OK).border(0, 6, 0, 0);
    add(label, BorderLayout.WEST);
    mem = new BaseXMem(main, true);
    add(mem, BorderLayout.EAST);
  }

  /**
   * Sets the status text.
   * @param txt the text to be set
   */
  public void setText(final String txt) {
    label.setText(txt);
    repaint();
  }

  /*@Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    // chop and print status text
    final FontMetrics fm = g.getFontMetrics();
    int fw = getWidth() - mem.getWidth() - 30;
    final StringBuilder sb = new StringBuilder(status);
    if(fm.stringWidth(status) > fw) {
      sb.setLength(0);
      for(int s = 0; s < status.length() && fw > 0; ++s) {
        sb.append(status.charAt(s));
        fw -= fm.charWidth(sb.charAt(s));
      }
      sb.append(DOTS);
    }
    g.drawString(sb.toString(), 4, getFont().getSize());
  }*/
}
