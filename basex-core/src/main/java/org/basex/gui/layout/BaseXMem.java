package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.basex.gui.dialog.*;
import org.basex.util.*;

/**
 * This component visualizes the current memory consumption.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXMem extends BaseXPanel {
  /** Default width of the memory status box. */
  private static final int DWIDTH = 70;

  /**
   * Constructor.
   * @param win window
   * @param mouse mouse interaction
   */
  public BaseXMem(final BaseXWindow win, final boolean mouse) {
    super(win);
    BaseXLayout.setWidth(this, DWIDTH);
    setPreferredSize(new Dimension(getPreferredSize().width, getFont().getSize() + 6));
    if(mouse) {
      setCursor(CURSORHAND);
      addMouseListener(this);
      addMouseMotionListener(this);
    }

    // regularly refresh panel
    new Timer(true).scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() { repaint(); }
    }, 0, 5000);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    final long total = rt.totalMemory();
    final long used = total - rt.freeMemory();
    final int ww = getWidth();
    final int hh = getHeight();

    // draw memory box
    g.setColor(BACK);
    g.fillRect(0, 0, ww - 3, hh - 3);
    g.setColor(gray);
    g.drawLine(0, 0, ww - 4, 0);
    g.drawLine(0, 0, 0, hh - 4);
    g.drawLine(ww - 3, 0, ww - 3, hh - 3);
    g.drawLine(0, hh - 3, ww - 3, hh - 3);

    // show total memory usage
    g.setColor(color1);
    g.fillRect(2, 2, Math.max(1, (int) (total * (ww - 6) / max)), hh - 6);

    // show current memory usage
    final boolean full = used * 6 / 5 > max;
    g.setColor(full ? colormark4 : color3);
    g.fillRect(2, 2, Math.max(1, (int) (used * (ww - 6) / max)), hh - 6);

    // print current memory usage
    final FontMetrics fm = g.getFontMetrics();
    final String mem = Performance.format(used);
    final int fw = (ww - fm.stringWidth(mem)) / 2;
    final int h = fm.getHeight() - 3;
    g.setColor(full ? colormark3 : dgray);
    g.drawString(mem, fw, h);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    DialogMem.show(gui);
    repaint();
  }
}
