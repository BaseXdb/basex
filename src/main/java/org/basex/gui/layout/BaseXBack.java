package org.basex.gui.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Fill;
import org.basex.util.Util;

/**
 * Panel background, extending the {@link JPanel}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXBack extends JPanel {
  /** Desktop hints. */
  private static final Map<?, ?> HINTS = (Map<?, ?>)
    Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
  /** Flag for adding rendering hints. */
  private static boolean hints = true;
  /** Fill mode. */
  private Fill mode;

  /**
   * Default constructor.
   */
  public BaseXBack() {
    this(Fill.PLAIN);
  }

  /**
   * Constructor, specifying the fill mode.
   * @param m visualization mode
   */
  public BaseXBack(final Fill m) {
    mode(m);
  }

  /**
   * Constructor, specifying an empty border.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   */
  public BaseXBack(final int t, final int l, final int b, final int r) {
    this();
    border(t, l, b, r);
  }

  /**
   * Constructor, specifying a layout manager.
   * @param lm layout manager
   */
  public BaseXBack(final LayoutManager lm) {
    this();
    layout(lm);
  }

  /**
   * Sets the specified fill mode.
   * @param m visualization mode
   * @return self reference
   */
  public final BaseXBack mode(final Fill m) {
    mode = m;
    final boolean o = mode != Fill.NONE;
    if(isOpaque() != o) setOpaque(o);
    return this;
  }

  @Override
  public void paintComponent(final Graphics g) {
    if(mode == Fill.GRADIENT) {
      final Color c1 = GUIConstants.WHITE;
      final Color c2 = GUIConstants.color1;
      BaseXLayout.fill(g, c1, c2, 0, 0, getWidth(), getHeight());
    } else {
      super.paintComponent(g);
    }

    // rendering hints are not supported by all platforms
    if(hints) {
      try {
        ((Graphics2D) g).addRenderingHints(HINTS);
      } catch(final Exception ex) {
        Util.debug(ex);
        hints = false;
      }
    }
  }

  /**
   * Sets an empty border with the specified margins.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public final BaseXBack border(final int t, final int l, final int b,
      final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
    return this;
  }

  /**
   * Sets an empty border with the specified margin.
   * @param m margin
   * @return self reference
   */
  public final BaseXBack border(final int m) {
    return border(m, m, m, m);
  }

  /**
   * Sets the layout manager for this container.
   * @param lm layout manager
   * @return self reference
   */
  public final BaseXBack layout(final LayoutManager lm) {
    setLayout(lm);
    return this;
  }

  /**
   * Activates graphics anti-aliasing.
   * @param g graphics reference
   */
  protected final void smooth(final Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
  }
}
