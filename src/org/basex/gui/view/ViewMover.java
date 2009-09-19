package org.basex.gui.view;

import static org.basex.core.Text.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXPanel;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class ViewMover extends BaseXPanel {
  /** Size of splitter. */
  private static final int SIZE = 8;
  /** Flag if current mover is active. */
  private boolean active;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  ViewMover(final GUI main) {
    super(HELPMOVER, main);
    setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), SIZE));
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setCursor(GUIConstants.CURSORMOVE);
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Color color1 = active ? GUIConstants.COLORS[4] :
      GUIConstants.color2;
    final Color color2 = active ? GUIConstants.COLORS[20] :
      GUIConstants.COLORS[10];

    final int w = getWidth();
    final int h = getHeight();
    g.setColor(color1);
    g.fillRect(0, 0, w, h);
    g.setColor(color2);
    for(int x = -4; x < w; x += 4) g.drawLine(x + 4, 1, x, h - 2);
    g.drawRect(0, 0, w - 1, h - 1);

    /*
    g.setColor(color2);
    g.fillRect(w - SIZE - 2, 0, SIZE + 2, SIZE);
    g.setColor(color1);
    g.drawLine(w - SIZE - 1, 0, w - SIZE - 1, SIZE);
    g.drawLine(w - SIZE + 1, 1, w - 2, SIZE - 2);
    g.drawLine(w - SIZE + 1, SIZE - 2, w - 2, 1);
    */
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
    Component comp = this;
    while(!((comp = comp.getParent()) instanceof ViewContainer));
    ((ViewContainer) comp).dropPanel();
    active = false;
    repaint();
  }
}
