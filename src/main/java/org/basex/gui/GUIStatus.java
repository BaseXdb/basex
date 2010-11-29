package org.basex.gui;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXMem;
import org.basex.gui.layout.BaseXPanel;
import org.basex.util.Token;

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
  /** Current status text. */
  private String status = OK;
  /** Current path. */
  private String oldStatus = OK;

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
    mem = new BaseXMem(main, true);
    add(mem, BorderLayout.EAST);
  }

  /**
   * Sets the status text.
   * @param stat the text to be set
   */
  public void setText(final String stat) {
    refresh(stat);
  }

  /**
   * Refreshes the status text.
   * @param txt status text
   */
  private void refresh(final String txt) {
    status = txt;
    oldStatus = status;
    repaint();
  }

  /**
   * Sets the current path.
   * @param path the path to be set
   */
  public void setPath(final byte[] path) {
    status = path.length == 0 ? oldStatus : Token.string(path);
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    // chop and print status text
    g.setColor(Color.black);
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
  }
}
