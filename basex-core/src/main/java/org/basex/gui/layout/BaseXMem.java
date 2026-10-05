package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Timer;

import org.basex.gui.dialog.*;
import org.basex.util.*;

/**
 * This component visualizes the current memory consumption.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BaseXMem extends BaseXPanel {
  /** Text sample for computing the width of the memory status box. */
  private static final String SAMPLE = "888.8 MB   88.8 GB";

  /** Timer for sampling the memory consumption. */
  private final Timer timer;
  /** Sampled maximum memory. */
  private long max;
  /** Sampled reserved memory. */
  private long total;
  /** Sampled used memory. */
  private long used;
  /** Displayed used memory. */
  private String usedText = "";

  /**
   * Constructor.
   * @param win window
   * @param mouse mouse interaction
   */
  public BaseXMem(final BaseXWindow win, final boolean mouse) {
    super(win);
    // dialogs show the progress of running operations
    timer = new Timer(win.dialog() != null ? 100 : 5000, e -> sample());
    final FontMetrics fm = getFontMetrics(getFont());
    setPreferredSize(new Dimension(fm.stringWidth(SAMPLE) + 16, getFont().getSize() + 8));
    if(mouse) {
      setCursor(CURSORHAND);
      addMouseListener(this);
      addMouseMotionListener(this);
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    sample();
    timer.start();
  }

  @Override
  public void removeNotify() {
    timer.stop();
    super.removeNotify();
  }

  /**
   * Samples the memory consumption and repaints the panel if the display has changed.
   */
  private void sample() {
    final Runtime rt = Runtime.getRuntime();
    final long mx = rt.maxMemory(), tt = rt.totalMemory(), us = tt - rt.freeMemory();
    final String ut = Performance.formatHuman(us);
    max = mx;
    total = tt;
    used = us;
    usedText = ut;
    repaint();
  }

  /**
   * Returns the width of a memory bar.
   * @param mem memory
   * @param mx maximum memory
   * @return width in pixels
   */
  private int width(final long mem, final long mx) {
    return mx == 0 ? 0 : (int) (mem * (getWidth() - 6) / mx);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    // draw memory box
    final int ww = getWidth(), hh = getHeight();
    g.setColor(backColor);
    g.fillRect(0, 0, ww - 3, hh - 3);
    g.setColor(gray);
    g.drawLine(0, 0, ww - 4, 0);
    g.drawLine(0, 0, 0, hh - 4);
    g.drawLine(ww - 3, 0, ww - 3, hh - 3);
    g.drawLine(0, hh - 3, ww - 3, hh - 3);

    // show total memory usage
    g.setColor(color1);
    g.fillRect(2, 2, Math.max(1, width(total, max)), hh - 6);

    // show current memory usage
    final boolean full = used * 6 / 5 > max;
    g.setColor(full ? colormark4 : color3);
    g.fillRect(2, 2, Math.max(1, width(used, max)), hh - 6);

    // print current memory usage: left-aligned at 0%, right-aligned at 100%
    final FontMetrics fm = g.getFontMetrics();
    final int space = ww - 14 - fm.stringWidth(usedText);
    final int x = 6 + (max == 0 ? 0 : (int) (Math.max(0, space) * Math.min(used, max) / max));
    final int h = (hh - 3 + fm.getAscent() - fm.getDescent()) / 2;
    g.setColor(full ? colormark3 : darkGray);
    g.drawString(usedText, x, h);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    DialogMem.show(gui);
    repaint();
  }
}
