package org.basex.gui.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * This panel is added to each view to allow drag and drop operations.
 *
 * @author BaseX Team, BSD License
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
    g.setColor(GUIConstants.panelColor);
    g.fillRect(0, 0, w, h);
    g.setColor(move || in ? GUIConstants.middleGray : GUIConstants.gray);
    for(int y = h - 1; y >= 0; y -= 2) g.drawLine(0, y, w, y);
  }

  /**
   * Returns the view container.
   * @return view container
   */
  private ViewContainer container() {
    return (ViewContainer) SwingUtilities.getAncestorOfClass(ViewContainer.class, this);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final ViewContainer container = container();
    container.dragPanel((ViewPanel) getParent(), SwingUtilities.convertPoint(this, e.getPoint(),
        container));
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
    container().dropPanel();
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
