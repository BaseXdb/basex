package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

/**
 * DoubleSlider implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXDSlider extends BaseXPanel {
  /** Label space (unscaled). */
  public static final int LABELW = 300;
  /** Slider width. */
  private static final int ARROW = 17;

  /** Minimum slider value. */
  public final double min;
  /** Maximum slider value. */
  public final double max;

  /** Current slider value. */
  public double currMin;
  /** Current slider value. */
  public double currMax;
  /** Integer flag. */
  public boolean itr;

  /** Listener. */
  private final ActionListener listener;
  /** Cached slider value. */
  private double oldMin;
  /** Cached slider value. */
  private double oldMax;
  /** Mouse position for dragging operations. */
  private int mouX;
  /** Left button flag. */
  private boolean left;
  /** Right button flag. */
  private boolean right;
  /** Right button flag. */
  private boolean center;
  /** Logarithmic scale. */
  private final boolean log;

  /**
   * Constructor.
   * @param win reference to the main window
   * @param min min value
   * @param max max value
   * @param listener listener
   */
  public BaseXDSlider(final BaseXWindow win, final double min, final double max,
      final ActionListener listener) {

    super(win);
    this.listener = listener;
    this.min = min;
    this.max = max;
    currMin = min;
    currMax = max;

    // choose logarithmic scaling for larger ranges
    log = StrictMath.log(max) - StrictMath.log(min) > 5 && max - min > 100;
    setOpaque(false);
    setFocusable(true);

    BaseXLayout.setWidth(this, 200 + LABELW);
    setPreferredSize(new Dimension(getPreferredSize().width, getFont().getSize() + 9));

    addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e) {
        repaint();
      }
      @Override
      public void focusLost(final FocusEvent e) {
        repaint();
      }
    });

    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setToolTip();
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    mouX = e.getX();
    final Range r = new Range(this);
    left = mouX >= r.xs && mouX <= r.xs + ARROW;
    right = mouX >= r.xe && mouX <= r.xe + ARROW;
    center = mouX + ARROW > r.xs && mouX < r.xe;
    oldMin = encode(currMin);
    oldMax = encode(currMax);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!left && !right && !center) return;

    final Range r = new Range(this);
    final double prop = r.dist * (mouX - e.getX()) / r.w;

    if(left) {
      currMin = limit(min, currMax, decode(oldMin - prop) - 1);
    } else if(right) {
      currMax = limit(currMin, max, decode(oldMax - prop) - 1);
    } else {
      currMin = limit(min, max, decode(oldMin - prop) - 1);
      currMax = limit(min, max, decode(oldMax - prop) - 1);
    }
    if(itr) {
      currMin = (long) currMin;
      currMax = (long) currMax;
    }
    listener.actionPerformed(null);
    setToolTip();
    repaint();
  }

  /**
   * Sets a new tooltip.
   */
  private void setToolTip() {
    final double mn = (long) (currMin * 100) / 100.0;
    final double mx = (long) (currMax * 100) / 100.0;
    setToolTipText(BaseXLayout.value(mn) + " - " + BaseXLayout.value(mx));
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    left = false;
    right = false;
    center = false;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    oldMin = currMin;
    oldMax = currMin;
    double diffMin = 0;
    double diffMax = 0;
    if(PREVCHAR.is(e)) {
      diffMin = -1;
      diffMax = -1;
    } else if(NEXTCHAR.is(e)) {
      diffMin = 1;
      diffMax = 1;
    } else if(PREVLINE.is(e)) {
      diffMin = -1;
      diffMax = 1;
    } else if(NEXTLINE.is(e)) {
      diffMin = 1;
      diffMax = -1;
    } else if(LINESTART.is(e)) {
      currMin = min;
    } else if(LINEEND.is(e)) {
      currMax = max;
    }
    if(e.isShiftDown()) {
      diffMin /= 10;
      diffMax /= 10;
    }

    final double dist = encode(max) - encode(min);
    diffMin = dist / 20 * diffMin;
    diffMax = dist / 20 * diffMax;

    if(diffMin != 0) {
      currMin = limit(min, currMax, decode(Math.max(0, encode(currMin) + diffMin)));
    }
    if(diffMax != 0) {
      currMax = limit(currMin, max, decode(Math.max(0, encode(currMax) + diffMax)));
    }
    if(currMin != oldMin || currMax != oldMax) {
      if(itr) {
        if(currMin != oldMin) currMin = currMin > oldMin ? Math.max(oldMin + 1,
            (long) currMin) : Math.min(oldMin - 1, (long) currMin);
        if(currMax != oldMax) currMax = currMax > oldMax ? Math.max(oldMax + 1,
            (long) currMax) : Math.min(oldMax - 1, (long) currMax);
      }
      listener.actionPerformed(null);
      repaint();
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final int w = getWidth() - LABELW;
    final int h = getHeight();
    final int hc = h / 2;
    final int s = 4;

    final boolean focus = hasFocus();
    g.setColor(BACK);
    g.fillRect(0, hc - s, w, s << 1);
    g.setColor(TEXT);
    g.drawLine(0, hc - s, w - 1, hc - s);
    g.drawLine(0, hc - s, 0, hc + s);
    g.setColor(color2);
    g.drawLine(w - 1, hc - s, w - 1, hc + s);
    g.drawLine(0, hc + s, w, hc + s);

    final Range r = new Range(this);
    BaseXLayout.drawCell(g, r.xs, r.xe + ARROW, 2, h - 2, false);

    if(r.xs + ARROW < r.xe) {
      g.setColor(color4);
      g.drawLine(r.xs + ARROW, 3, r.xs + ARROW, h - 4);
      g.drawLine(r.xe - 1, 3, r.xe - 1, h - 4);
      g.setColor(BACK);
      if(r.xs + ARROW + 2 < r.xe) {
        g.drawLine(r.xs + ARROW + 1, 4, r.xs + ARROW + 1, h - 5);
        g.drawLine(r.xe, 4, r.xe, h - 5);
      }
      g.drawLine(r.xs + ARROW - 1, 4, r.xs + ARROW - 1, h - 5);
      g.drawLine(r.xe - 2, 4, r.xe - 2, h - 5);
    }

    // draw arrows
    final Polygon pol = new Polygon(
        new int[] { r.xs + 11, r.xs + 5, r.xs + 5, r.xs + 11 },
        new int[] { hc - 5, hc - 1, hc, hc + 5 }, 4);
    g.setColor(focus ? color4 : gray);
    g.fillPolygon(pol);
    pol.xpoints = new int[] { r.xe + 5, r.xe + 12, r.xe + 12, r.xe + 5 };
    g.fillPolygon(pol);

    g.setColor(focus ? TEXT : dgray);
    g.drawLine(r.xs + 11, hc - 5, r.xs + 11, hc + 4);
    g.drawLine(r.xs + 11, hc - 5, r.xs + 6, hc - 1);
    g.drawLine(r.xe + 5, hc - 5, r.xe + 5, hc + 4);
    g.drawLine(r.xe + 5, hc - 5, r.xe + 11, hc - 1);

    g.setColor(BACK);
    g.drawLine(r.xs + 10, hc + 4, r.xs + 6, hc + 1);
    g.drawLine(r.xe + 6, hc + 4, r.xe + 11, hc + 1);

    // draw range info
    g.setColor(TEXT);
    final double mn = (long) (currMin * 100) / 100.0;
    final double mx = (long) (currMax * 100) / 100.0;

    g.drawString(BaseXLayout.value(mn) + " - " + BaseXLayout.value(mx),
        w + 15, h - (h - getFont().getSize()) / 2);
  }

  /**
   * Encodes the specified value.
   * @param v value to be normalized
   * @return new value
   */
  private double encode(final double v) {
    return log ? StrictMath.log(v + 1) : v;
  }

  /**
   * Decodes the specified value.
   * @param v value to be normalized
   * @return new value
   */
  private double decode(final double v) {
    return log ? StrictMath.exp(v) - 1 : v;
  }

  /**
   * Returns a double in the specified minimum and maximum range.
   * @param mn minimum value
   * @param mx maximum value
   * @param val value
   * @return new value
   */
  private static double limit(final double mn, final double mx, final double val) {
    return Math.max(mn, Math.min(mx, val));
  }

  /** Range class. */
  private static class Range {
    /** Range distance. */
    final double dist;
    /** Start position. */
    final int xs;
    /** End position.   */
    final int xe;
    /** Slider width.   */
    final int w;

    /**
     * Constructor.
     * @param s slider reference
     */
    Range(final BaseXDSlider s) {
      w = s.getWidth() - LABELW - (ARROW << 1);
      dist = s.encode(s.max - s.min);
      xs = (int) (s.encode(s.currMin - s.min) * w / dist);
      xe = (s.min == s.max ? w : (int) (s.encode(s.currMax - s.min) * w / dist)) + ARROW;
    }
  }
}
