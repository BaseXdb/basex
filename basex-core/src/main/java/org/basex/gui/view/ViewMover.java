package org.basex.gui.view;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ViewMover extends BaseXPanel {
  /** Panel height. */
  private int height;
  /** Flag if current mover is active. */
  private boolean active;
  /** Indicates if cursor is placed inside the mover. */
  private boolean in;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  ViewMover(final AGUI main) {
    super(main);
    setLayout(new BorderLayout());
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setCursor(GUIConstants.CURSORMOVE);
    refreshLayout();

    new BaseXPopup(this, new GUIPopupCmd(Text.CLOSE) {
      @Override
      public void execute() { ((ViewPanel) getParent()).delete(); }
    });
   }

  @Override
  public void paintComponent(final Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    final int w = getWidth();
    final int h = getHeight();
    g.setColor(GUIConstants.color(active ? 5 : in ? 3 : 1));
    g.fillRect(0, 0, w, h);
    g.setColor(GUIConstants.color(active ? 16 : in ? 13 : 10));
    final int d = height >> 1;
    for(int x = -d >> 1; x < w; x += 2 + (height >> 2)) g.drawLine(x + d, 0, x, h - 1);
    g.drawRect(0, 0, w - 1, h - 1);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final ViewPanel view = (ViewPanel) getParent();
    Component comp = view;
    while(!((comp = comp.getParent()) instanceof ViewContainer));

    final Point a = getLocationOnScreen();
    final Point b = comp.getLocationOnScreen();
    final Point c = new Point(a.x - b.x + e.getX(), a.y - b.y + e.getY());
    ((ViewContainer) comp).dragPanel(view, c);
    active = true;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    active = true;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(!active) return;
    Component comp = this;
    while(!((comp = comp.getParent()) instanceof ViewContainer));
    ((ViewContainer) comp).dropPanel();
    active = false;
    repaint();
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    in = true;
    repaint();
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    in = false;
    repaint();
  }

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    height = Math.max(10, 6 + (int) (GUIConstants.fontSize * 0.333));
    setPreferredSize(new Dimension(getPreferredSize().width, height));
  }
}
