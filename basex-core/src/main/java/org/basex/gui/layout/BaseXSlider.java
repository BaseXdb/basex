package org.basex.gui.layout;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;
import org.basex.util.options.*;

/**
 * Project specific slider implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXSlider extends BaseXPanel {
  /** Default width of slider. */
  private static final int DWIDTH = 120;
  /** Width of slider mover. */
  private static final double SLIDERW = 20;
  /** Listener. */
  private final BaseXDialog dialog;
  /** Minimum slider value. */
  private final int min;
  /** Maximum slider value. */
  private final int max;

  /** Options. */
  private Options options;
  /** Number option. */
  private NumberOption option;

  /** Current slider value. */
  private int value;
  /** Old slider value. */
  private int oldValue = -1;
  /** Mouse position for dragging operations. */
  private int mouseX;

  /**
   * Checkbox.
   * @param win parent window
   * @param min min value
   * @param max max value
   * @param option option
   * @param options options
   */
  public BaseXSlider(final BaseXWindow win, final int min, final int max, final NumberOption option,
      final Options options) {
    this(win, min, max, options.get(option));
    this.options = options;
    this.option = option;
  }

  /**
   * Constructor.
   * @param win parent window
   * @param min min value
   * @param max max value
   * @param value initial value
   */
  public BaseXSlider(final BaseXWindow win, final int min, final int max, final int value) {
    super(win);
    this.min = min;
    this.max = max;
    this.value = value;
    dialog = win.dialog();

    setOpaque(false);
    setFocusable(true);

    setPreferredSize(new Dimension(DWIDTH, (int) (getFont().getSize() * 1.2)));
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
  }

  /**
   * Returns the current value.
   * @return current value
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets a slider value.
   * @param v new value
   */
  public void setValue(final int v) {
    value = v;
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final int w = getWidth();
    final int h = getHeight();
    final int hh = h / 2;
    final int s = 3;

    g.setColor(hasFocus() ? GUIConstants.BACK : GUIConstants.lgray);
    g.fillRect(0, hh - s, w, (s << 1) - 1);
    g.setColor(GUIConstants.TEXT);
    g.drawLine(0, hh - s, w, hh - s);
    g.drawLine(0, hh - s, 0, hh + s - 1);
    g.setColor(GUIConstants.gray);
    g.drawLine(w - 1, hh - s, w - 1, hh + s - 1);
    g.drawLine(0, hh + s - 1, w, hh + s - 1);

    final double x = (value - min) * (w - SLIDERW) / (max - min);
    BaseXLayout.drawCell(g, (int) x, (int) (x + SLIDERW), hh - (s << 1), hh + (s << 1),
        oldValue != -1);
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
    final double x = (value - min) * w / r;
    if(mouseX < x || mouseX >= x + SLIDERW) {
      value = (int) (mouseX * r / w + min);
      notifyListeners();
    }
    oldValue = value;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    oldValue = -1;
    repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final double prop = (max - min) * (mouseX - e.getX()) /
      (getWidth() - SLIDERW);

    final int old = value;
    value = Math.max(min, Math.min(max, (int) (oldValue - prop)));
    if(value != old) notifyListeners();
    repaint();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int old = value;
    if(PREVCHAR.is(e) || PREVLINE.is(e)) {
      value = Math.max(min, value - 1);
    } else if(NEXTCHAR.is(e) || NEXTLINE.is(e)) {
      value = Math.min(max, value + 1);
    } else if(NEXTPAGE.is(e)) {
      value = Math.max(min, value + 10);
    } else if(PREVPAGE.is(e)) {
      value = Math.min(max, value - 10);
    } else if(LINESTART.is(e)) {
      value = min;
    } else if(LINEEND.is(e)) {
      value = max;
    }
    if(value != old) notifyListeners();
    repaint();
  }

  /**
   * Notifies all listeners.
   */
  private void notifyListeners() {
    if(dialog != null) dialog.action(null);
    for(final ActionListener al : listenerList.getListeners(ActionListener.class)) {
      al.actionPerformed(null);
    }
  }

  /**
   * Adds an action listener.
   * @param l listener
   */
  public void addActionListener(final ActionListener l) {
    listenerList.add(ActionListener.class, l);
  }

  /**
   * Assigns the current checkbox value to the option specified in the constructor.
   */
  public void assign() {
    options.set(option, value);
  }
}
