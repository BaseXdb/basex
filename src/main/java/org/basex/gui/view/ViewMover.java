package org.basex.gui.view;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import org.basex.gui.AGUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class ViewMover extends BaseXPanel {
  /** Size of splitter. */
  private static final int SIZE = 8;
  /** Flag if current mover is active. */
  boolean active;
  /** Close button. */
  final BaseXButton close;

  /**
   * Constructor.
   * @param main reference to the main window
   */
  ViewMover(final AGUI main) {
    super(HELPMOVER, main);
    this.setLayout(new BorderLayout());
    BaseXLayout.setHeight(this, SIZE);
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setCursor(GUIConstants.CURSORMOVE);

    close = new BaseXButton(gui, "editclose", null);
    close.setRolloverIcon(BaseXLayout.icon("cmd-editclose2"));
    close.border(2, 2, 2, 2);
    close.setContentAreaFilled(false);
    close.setFocusable(false);
    close.setVisible(false);
    close.setCursor(GUIConstants.CURSORARROW);
    close.addMouseListener(this);
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        active = false;
        close.setVisible(false);
        ((ViewPanel) getParent()).delete();
      }
    });

    add(close, BorderLayout.EAST);
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

  @Override
  public void mouseEntered(final MouseEvent e) {
    close.setVisible(true);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    // this method is called, if the mouse enters the close button and the
    // absolute position of the mouse should be checked
    final int x = e.getXOnScreen();
    final int y = e.getYOnScreen();
    final Point p = getLocationOnScreen();
    if(x < p.x || y < p.y || x >= p.x + getWidth() || y >= p.y + getHeight())
      close.setVisible(false);
  }
}
