package org.basex.gui.layout;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Panel background, extending the {@link JPanel}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class BaseXBack extends JPanel {
  /** Size of visual separators. */
  public static final int SEPARATOR_SIZE = 8;

  /**
   * Default constructor.
   */
  public BaseXBack() {
  }

  /**
   * Constructor, specifying the opaque flag.
   * @param opaque opaque flag
   */
  public BaseXBack(final boolean opaque) {
    setOpaque(opaque);
  }

  /**
   * Constructor, specifying a layout manager.
   * @param lm layout manager
   */
  public BaseXBack(final LayoutManager lm) {
    setLayout(lm);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    BaseXLayout.hints(g);
  }

  /**
   * Sets an empty border with the specified margins.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public final BaseXBack border(final int t, final int l, final int b, final int r) {
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
}
