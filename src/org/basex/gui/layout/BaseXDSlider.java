package org.basex.gui.layout;

import static org.basex.gui.GUIConstants.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.gui.GUI;

/**
 * DoubleSlider implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseXDSlider extends BaseXPanel {
  /** Slider width. */
  public static final int ARROW = 17;
  /** Label space. */
  public static final int LABELW = 300;

  /** Current slider value. */
  public double min;
  /** Current slider value. */
  public double max;
  /** Minimum slider value. */
  public double totMin;
  /** Maximum slider value. */
  public double totMax;
  /** Size representation in kilobytes. */
  public boolean kb;
  /** Size representation as date. */
  public boolean date;
  /** Integer flag. */
  public boolean itr;
  /** Logarithmic scale. */
  public boolean log;

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

  /**
   * Constructor.
   * @param main reference to the main window
   * @param mn min value
   * @param mx max value
   * @param list listener
   */
  public BaseXDSlider(final GUI main, final double mn, final double mx,
      final ActionListener list) {
    super(main);
    listener = list;
    totMin = mn;
    totMax = mx;
    min = mn;
    max = mx;
    // choose logarithmic scaling for larger ranges
    log = Math.log(totMax) - Math.log(totMin) > 5 && totMax - totMin > 100;
    setFocusable(true);
    setMode(Fill.NONE);

    BaseXLayout.setHeight(this, getFont().getSize() + 9);
    BaseXLayout.setWidth(this, 200 + LABELW);

    addFocusListener(new FocusAdapter() {
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

  /**
   * Formats a number according to the binary size orders (KB, MB, ...).
   * @param size value to be formatted
   * @return formatted size value
   */
  public String value(final double size) {
    return BaseXLayout.value(size, kb, date);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    mouX = e.getX();
    final Range r = new Range();
    left = mouX >= r.xs && mouX <= r.xs + ARROW;
    right = mouX >= r.xe && mouX <= r.xe + ARROW;
    center = mouX + ARROW > r.xs && mouX < r.xe;
    oldMin = encode(min);
    oldMax = encode(max);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!left && !right && !center) return;

    final Range r = new Range();
    final double prop = r.dist * (mouX - e.getX()) / r.w;

    if(left) {
      min = limit(totMin, max, decode(oldMin - prop) - 1);
    } else if(right) {
      max = limit(min, totMax, decode(oldMax - prop) - 1);
    } else {
      min = limit(totMin, totMax, decode(oldMin - prop) - 1);
      max = limit(totMin, totMax, decode(oldMax - prop) - 1);
    }
    if(itr) {
      min = (long) min;
      max = (long) max;
    }
    listener.actionPerformed(null);
    setToolTip();
    repaint();
  }

  /**
   * Sets a new tooltip.
   */
  private void setToolTip() {
    final double mn = (long) (min * 100) / 100.0;
    final double mx = (long) (max * 100) / 100.0;
    setToolTipText(value(mn) + " - " + value(mx));
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    left = false;
    right = false;
    center = false;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(e.isAltDown()) return;

    final int code = e.getKeyCode();
    oldMin = min;
    oldMax = min;
    double diffMin = 0;
    double diffMax = 0;
    if(code == KeyEvent.VK_LEFT) {
      diffMin = -1;
      diffMax = -1;
    } else if(code == KeyEvent.VK_RIGHT) {
      diffMin = 1;
      diffMax = 1;
    } else if(code == KeyEvent.VK_UP) {
      diffMin = -1;
      diffMax = 1;
    } else if(code == KeyEvent.VK_DOWN) {
      diffMin = 1;
      diffMax = -1;
    } else if(code == KeyEvent.VK_HOME) {
      min = totMin;
    } else if(code == KeyEvent.VK_END) {
      max = totMax;
    }

    if(e.isShiftDown()) {
      diffMin /= 10;
      diffMax /= 10;
    }
    if(e.isControlDown()) {
      diffMin /= 100;
      diffMax /= 100;
    }
    final double dist = encode(totMax) - encode(totMin);
    //final double dist = encode(totalMax - totalMin);
    diffMin = dist / 20 * diffMin;
    diffMax = dist / 20 * diffMax;

    if(diffMin != 0) {
      min = limit(totMin, max, decode(Math.max(0, encode(min) + diffMin)));
    }
    if(diffMax != 0) {
      max = limit(min, totMax, decode(Math.max(0, encode(max) + diffMax)));
    }
    if(min != oldMin || max != oldMax) {
      if(itr) {
        if(min != oldMin) min = min > oldMin ? Math.max(oldMin + 1,
            (long) min) : Math.min(oldMin - 1, (long) min);
        if(max != oldMax) max = max > oldMax ? Math.max(oldMax + 1,
            (long) max) : Math.min(oldMax - 1, (long) max);
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
    final int hh = h / 2;

    final boolean focus = hasFocus();
    g.setColor(focus ? Color.white : color1);
    g.fillRect(0, hh - 4, w, 8);
    g.setColor(Color.black);
    g.drawLine(0, hh - 4, w - 1, hh - 4);
    g.drawLine(0, hh - 4, 0, hh + 4);
    g.setColor(color3);
    g.drawLine(w - 1, hh - 4, w - 1, hh + 4);
    g.drawLine(0, hh + 4, w, hh + 4);

    final Range r = new Range();
    BaseXLayout.drawCell(g, r.xs, r.xe + ARROW, 2, h - 2, false);

    if(r.xs + ARROW < r.xe) {
      g.setColor(color6);
      g.drawLine(r.xs + ARROW, 3, r.xs + ARROW, h - 4);
      g.drawLine(r.xe - 1, 3, r.xe - 1, h - 4);
      g.setColor(Color.white);
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
        new int[] { hh - 5, hh - 1, hh, hh + 5 }, 4);
    g.setColor(focus ? color6 : COLORBUTTON);
    g.fillPolygon(pol);
    pol.xpoints = new int[] { r.xe + 5, r.xe + 12, r.xe + 12, r.xe + 5 };
    g.fillPolygon(pol);

    g.setColor(focus ? Color.black : COLORDARK);
    g.drawLine(r.xs + 11, hh - 5, r.xs + 11, hh + 4);
    g.drawLine(r.xs + 11, hh - 5, r.xs + 6, hh - 1);
    g.drawLine(r.xe + 5, hh - 5, r.xe + 5, hh + 4);
    g.drawLine(r.xe + 5, hh - 5, r.xe + 11, hh - 1);

    g.setColor(Color.white);
    g.drawLine(r.xs + 10, hh + 4, r.xs + 6, hh + 1);
    g.drawLine(r.xe + 6, hh + 4, r.xe + 11, hh + 1);

    // draw range info
    g.setColor(Color.black);
    final double mn = (long) (min * 100) / 100.0;
    final double mx = (long) (max * 100) / 100.0;

    g.drawString(value(mn) + " - " + value(mx), w + 15,
        h - (h - getFont().getSize()) / 2);
  }

  /**
   * Encodes the specified value.
   * @param v value to be normalized
   * @return new value
   */
  double encode(final double v) {
    return log ? Math.log(v + 1) : v;
  }

  /**
   * Decodes the specified value.
   * @param v value to be normalized
   * @return new value
   */
  double decode(final double v) {
    return log ? Math.exp(v) - 1 : v;
  }

  /**
   * Returns a double in the specified minimum and maximum range.
   * @param mn minimum value
   * @param mx maximum value
   * @param val value
   * @return new value
   */
  private double limit(final double mn, final double mx, final double val) {
    return Math.max(mn, Math.min(mx, val));
  }

  /** Range class. */
  class Range {
    /** Range distance. */ double dist;
    /** Start position. */ int xs;
    /** End position.   */ int xe;
    /** Slider Width.   */ int w;

    /** Constructor. */
    Range() {
      w = getWidth() - LABELW - ARROW * 2;
      dist = encode(totMax - totMin);
      xs = (int) (encode(min - totMin) * w / dist);
      xe = (totMin == totMax ? w :
        (int) (encode(max - totMin) * w / dist)) + ARROW;
    }
  }
}
