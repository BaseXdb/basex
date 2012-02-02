package org.basex.gui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.basex.gui.AGUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class ViewMover extends BaseXPanel {
  /** Size of splitter. */
  private static final int SIZE = 9;
  /** Flag if current mover is active. */
  private boolean active;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  ViewMover(final AGUI main) {
    super(main);
    setLayout(new BorderLayout());
    BaseXLayout.setHeight(this, SIZE);
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setCursor(GUIConstants.CURSORMOVE);
  }

  @Override
  public void paintComponent(final Graphics g) {
    final int w = getWidth();
    final int h = getHeight();
    g.setColor(GUIConstants.color(active ? 4 : 1));
    g.fillRect(0, 0, w, h);
    g.setColor(GUIConstants.color(active ? 20 : 10));
    for(int x = -2; x < w; x += 4) g.drawLine(x + 4, 1, x, h - 2);
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
}
