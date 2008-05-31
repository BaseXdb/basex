package org.basex.gui.layout;

import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.basex.gui.GUIConstants.FILL;

/**
 * Panel background, extending the {@link JPanel}.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BaseXBack extends JPanel {
  /** Design of color gradient. */
  private FILL mode;
  
  /**
   * Default Constructor.
   */
  public BaseXBack() {
    this(FILL.PLAIN);
  }
  
  /**
   * Default Constructor.
   * @param m visualization mode
   */
  public BaseXBack(final FILL m) {
    setMode(m);
  }
  
  /**
   * Default Constructor.
   * @param m visualization mode
   */
  public final void setMode(final FILL m) {
    mode = m;
    final boolean o = mode != FILL.NONE;
    if(isOpaque() != o) setOpaque(o);
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(mode == FILL.UP || mode == FILL.DOWN) {
      BaseXLayout.fill(g, mode, 0, 0, getWidth(), getHeight());
    }
  }

  /**
   * Set an empty border.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   */
  public final void setBorder(final int t, final int l, final int b,
      final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
  }
}
