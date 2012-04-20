package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.MouseEvent;
import org.basex.util.Performance;

/**
 * This component visualizes the current memory consumption.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXMem extends BaseXPanel {
  /** Default width of the memory status box. */
  private static final int DWIDTH = 70;

  /**
   * Constructor.
   * @param win parent reference
   * @param mouse mouse interaction
   */
  public BaseXMem(final Window win, final boolean mouse) {
    super(win);
    BaseXLayout.setWidth(this, DWIDTH);
    BaseXLayout.setHeight(this, getFont().getSize() + 6);
    if(mouse) {
      setCursor(CURSORHAND);
      addMouseListener(this);
      addMouseMotionListener(this);
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    final long used = rt.totalMemory() - rt.freeMemory();
    final int ww = getWidth();
    final int hh = getHeight();

    // draw memory box
    g.setColor(Color.white);
    g.fillRect(0, 0, ww - 3, hh - 3);
    g.setColor(GRAY);
    g.drawLine(0, 0, ww - 4, 0);
    g.drawLine(0, 0, 0, hh - 4);
    g.drawLine(ww - 3, 0, ww - 3, hh - 3);
    g.drawLine(0, hh - 3, ww - 3, hh - 3);

    // show current memory usage
    final boolean full = used * 6 / 5 > max;
    g.setColor(full ? colormark4 : color1);
    g.fillRect(2, 2, Math.max(1, (int) (used * (ww - 6) / max)), hh - 6);

    // print current memory usage
    final FontMetrics fm = g.getFontMetrics();
    final String mem = Performance.format(used, true);
    final int fw = (ww - fm.stringWidth(mem)) / 2;
    final int h = fm.getHeight() - 3;
    g.setColor(full ? colormark3 : DGRAY);
    g.drawString(mem, fw, h);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    Performance.gc(3);
    repaint();

    final Runtime rt = Runtime.getRuntime();
    final long occ = rt.totalMemory();
    final long max = rt.maxMemory();
    final long used = occ - rt.freeMemory();

    final String inf = TOTAL_MEM_C + Performance.format(max, true) + NL
        + RESERVED_MEM_C + Performance.format(occ, true) + NL + MEMUSED_C
        + Performance.format(used, true) + NL + NL + H_USED_MEM;

    BaseXDialog.info(gui, inf);
  }
}
