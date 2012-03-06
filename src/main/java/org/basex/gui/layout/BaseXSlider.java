package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.dialog.Dialog;

/**
 * Project specific slider implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXSlider extends BaseXPanel {
  /** Default width of slider. */
  private static final int DWIDTH = 120;
  /** Slider width. */
  private static final double SLIDERW = 20;
  /** Listener. */
  private final Dialog dl;
  /** Minimum slider value. */
  private final int min;
  /** Maximum slider value. */
  private final int max;
  /** Listener. */
  private final ActionListener al;

  /** Current slider value. */
  private int curr;
  /** Current slider value. */
  private int oldCurr = -1;
  /** Mouse position for dragging operations. */
  private int mouseX;

  /**
   * Constructor.
   * @param mn min value
   * @param mx max value
   * @param i initial value
   * @param w parent window
   */
  public BaseXSlider(final int mn, final int mx, final int i, final Window w) {
    this(mn, mx, i, w, null);
  }

  /**
   * Constructor.
   * @param mn min value
   * @param mx max value
   * @param i initial value
   * @param w reference to the main window
   * @param list listener
   */
  public BaseXSlider(final int mn, final int mx, final int i, final Window w,
      final ActionListener list) {

    super(w);
    min = mn;
    max = mx;
    curr = i;
    dl = w instanceof Dialog ? (Dialog) w : null;
    al = list;
    mode(Fill.NONE).setFocusable(true);

    setPreferredSize(new Dimension(DWIDTH, getFont().getSize() + 3));
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
  }

  /**
   * Returns the current value.
   * @return current value
   */
  public int value() {
    return curr;
  }

  /**
   * Sets a slider value.
   * @param v new value
   */
  public void value(final int v) {
    curr = v;
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final int w = getWidth();
    final int h = getHeight();
    final int hh = h / 2;

    g.setColor(hasFocus() ? Color.white : GUIConstants.LGRAY);
    g.fillRect(0, hh - 3, w, 4);
    g.setColor(Color.black);
    g.drawLine(0, hh - 3, w, hh - 3);
    g.drawLine(0, hh - 3, 0, hh + 2);
    g.setColor(GUIConstants.GRAY);
    g.drawLine(w - 1, hh - 3, w - 1, hh + 2);
    g.drawLine(0, hh + 2, w, hh + 2);

    final double x = (curr - min) * (w - SLIDERW) / (max - min);
    BaseXLayout.drawCell(g, (int) x, (int) (x + SLIDERW), hh - 6, hh + 6, oldCurr != -1);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    mouseX = e.getX();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    requestFocusInWindow();
    mouseX = e.getX();
    final double w = getWidth() - SLIDERW;
    final double r = max - min;
    final double x = (curr - min) * w / r;
    if(mouseX < x || mouseX >= x + SLIDERW) curr = (int) (mouseX * r / w + min);
    oldCurr = curr;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    oldCurr = -1;
    repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final double prop = (max - min) * (mouseX - e.getX()) /
      (getWidth() - SLIDERW);

    final int old = curr;
    curr = Math.max(min, Math.min(max, (int) (oldCurr - prop)));

    if(curr != old) {
      if(dl != null) dl.action(null);
      else al.actionPerformed(null);
      repaint();
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int old = curr;
    if(PREV.is(e) || PREVLINE.is(e)) {
      curr = Math.max(min, curr - 1);
    } else if(NEXT.is(e) || NEXTLINE.is(e)) {
      curr = Math.min(max, curr + 1);
    } else if(NEXTPAGE.is(e)) {
      curr = Math.max(min, curr + 10);
    } else if(PREVPAGE.is(e)) {
      curr = Math.min(max, curr - 10);
    } else if(LINESTART.is(e)) {
      curr = min;
    } else if(LINEEND.is(e)) {
      curr = max;
    }
    if(curr != old) {
      if(dl != null) dl.action(null);
      else al.actionPerformed(null);
      repaint();
    }
  }
}
