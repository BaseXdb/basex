package org.basex.gui.view;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ViewMover extends BaseXPanel {
  /** Flag if current mover is active. */
  private boolean move;
  /** Indicates if cursor is placed inside the mover. */
  private boolean in;

  /**
   * Constructor.
   * @param gui reference to the main window
   */
  ViewMover(final GUI gui) {
    super(gui);
    setLayout(new BorderLayout());
    addKeyListener(this);
    addMouseListener(this);
    addMouseMotionListener(this);
    setCursor(GUIConstants.CURSORMOVE);
    setPreferredSize(new Dimension(getPreferredSize().width, SEPARATOR_SIZE));

    new BaseXPopup(this, new GUIPopupCmd(Text.CLOSE) {
      @Override
      public void execute() { ((ViewPanel) getParent()).delete(); }
    });
   }

  @Override
  public void paintComponent(final Graphics g) {
    final int w = getWidth(), h = getHeight();
    g.setColor(GUIConstants.PANEL);
    g.fillRect(0, 0, w, h);
    g.setColor(move || in ? GUIConstants.mgray : GUIConstants.gray);
    for(int y = h - 1; y >= 0; y -= 2) g.drawLine(0, y, w, y);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final ViewPanel view = (ViewPanel) getParent();
    Component comp = view;
    while(!((comp = comp.getParent()) instanceof ViewContainer));

    final Point a = getLocationOnScreen(), b = comp.getLocationOnScreen();
    final Point c = new Point(a.x - b.x + e.getX(), a.y - b.y + e.getY());
    ((ViewContainer) comp).dragPanel(view, c);
    move = true;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    move = true;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(!move) return;
    Component comp = this;
    while(!((comp = comp.getParent()) instanceof ViewContainer));
    ((ViewContainer) comp).dropPanel();
    move = false;
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
}
