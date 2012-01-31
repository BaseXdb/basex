package org.basex.gui.layout;

import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.basex.gui.GUI;
import org.basex.gui.dialog.Dialog;

/**
 * Abstract panel implementation with a number of predefined listeners.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXPanel extends BaseXBack implements MouseListener,
    MouseMotionListener, ComponentListener, KeyListener, MouseWheelListener {

  /** Reference to the main window. */
  public final GUI gui;

  /**
   * Constructor, setting default interactions.
   * @param win parent reference, {@link Dialog} or {@link GUI} instance
   */
  protected BaseXPanel(final Window win) {
    gui = win instanceof GUI ? (GUI) win : ((Dialog) win).gui;
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
