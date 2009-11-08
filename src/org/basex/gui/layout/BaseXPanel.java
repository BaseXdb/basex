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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class BaseXPanel extends BaseXBack implements MouseListener,
    MouseMotionListener, ComponentListener, KeyListener, MouseWheelListener {

  /** Reference to the main window. */
  public final GUI gui;

  /**
   * Constructor, setting the help text.
   * @param win parent reference, {@link Dialog} or {@link GUI} instance
   */
  protected BaseXPanel(final Window win) {
    this(null, win);
  }

  /**
   * Constructor, setting the help text.
   * @param hlp help text
   * @param win parent reference, {@link Dialog} or {@link GUI} instance
   */
  protected BaseXPanel(final byte[] hlp, final Window win) {
    gui = win instanceof GUI ? (GUI) win : ((Dialog) win).gui;
    BaseXLayout.addInteraction(this, hlp, win);
  }

  public void mouseEntered(final MouseEvent e) { }
  public void mousePressed(final MouseEvent e) { }
  public void mouseReleased(final MouseEvent e) { }
  public void mouseClicked(final MouseEvent e) { }
  public void mouseExited(final MouseEvent e) { }
  public void mouseMoved(final MouseEvent e) { }
  public void mouseDragged(final MouseEvent e) { }

  public void keyPressed(final KeyEvent e) { }
  public void keyTyped(final KeyEvent e) { }
  public void keyReleased(final KeyEvent e) { }

  public void componentResized(final ComponentEvent e) { }
  public void componentHidden(final ComponentEvent e) { }
  public void componentShown(final ComponentEvent e) { }
  public void componentMoved(final ComponentEvent e) { }

  public void mouseWheelMoved(final MouseWheelEvent e) { }
}
