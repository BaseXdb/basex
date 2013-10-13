package org.basex.gui.layout;

import java.awt.*;
import java.awt.event.*;

import org.basex.gui.*;

/**
 * Abstract panel implementation with a number of predefined listeners.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXPanel extends BaseXBack implements MouseListener,
    MouseMotionListener, ComponentListener, KeyListener, MouseWheelListener {

  /** Reference to the main window. */
  public final GUI gui;

  /**
   * Constructor, setting default interactions.
   * @param win parent reference, {@link BaseXDialog} or {@link GUI} instance
   */
  protected BaseXPanel(final Window win) {
    gui = win instanceof GUI ? (GUI) win : ((BaseXDialog) win).gui;
    BaseXLayout.addInteraction(this, win);
  }

  @Override
  public void mouseEntered(final MouseEvent e) { }
  @Override
  public void mousePressed(final MouseEvent e) { }
  @Override
  public void mouseReleased(final MouseEvent e) { }
  @Override
  public void mouseClicked(final MouseEvent e) { }
  @Override
  public void mouseExited(final MouseEvent e) { }
  @Override
  public void mouseMoved(final MouseEvent e) { }
  @Override
  public void mouseDragged(final MouseEvent e) { }

  @Override
  public void keyPressed(final KeyEvent e) { }
  @Override
  public void keyTyped(final KeyEvent e) { }
  @Override
  public void keyReleased(final KeyEvent e) { }

  @Override
  public void componentResized(final ComponentEvent e) { }
  @Override
  public void componentHidden(final ComponentEvent e) { }
  @Override
  public void componentShown(final ComponentEvent e) { }
  @Override
  public void componentMoved(final ComponentEvent e) { }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) { }
}
