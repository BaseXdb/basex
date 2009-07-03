package org.basex.gui.layout;

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
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;

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
   * @param main reference to the main window
   * @param hlp help text
   */
  protected BaseXPanel(final GUI main, final byte[] hlp) {
    BaseXLayout.addHelp(this, hlp);
    gui = main;
  }

  public void mouseEntered(final MouseEvent e) { }
  public void mousePressed(final MouseEvent e) { }
  public void mouseReleased(final MouseEvent e) { }
  public void mouseClicked(final MouseEvent e) { }
  public void mouseExited(final MouseEvent e) { }
  public void mouseMoved(final MouseEvent e) { }
  public void mouseDragged(final MouseEvent e) { }

  public void keyPressed(final KeyEvent e) {
    if(gui.updating || !e.isAltDown()) return;

    final int key = e.getKeyCode();
    if(key == KeyEvent.VK_LEFT) {
      GUICommands.GOBACK.execute(gui);
    } else if(key == KeyEvent.VK_RIGHT) {
      GUICommands.GOFORWARD.execute(gui);
    } else if(key == KeyEvent.VK_UP) {
      GUICommands.GOUP.execute(gui);
    } else if(key == KeyEvent.VK_HOME) {
      GUICommands.ROOT.execute(gui);
    }
  }

  public void keyTyped(final KeyEvent e) {
    if(gui.updating || !e.isControlDown()) return;
    final char key = e.getKeyChar();
    if(key == '+' || key == '-' || key == '=') {
      GUIProp.fontsize = Math.max(1, GUIProp.fontsize + (key == '-' ? -1 : 1));
      gui.notify.layout();
    } else if(key == '0') {
      GUIProp.fontsize = 12;
      gui.notify.layout();
    }

  }
  public void keyReleased(final KeyEvent e) { }

  public void componentResized(final ComponentEvent e) { }
  public void componentHidden(final ComponentEvent e) { }
  public void componentShown(final ComponentEvent e) { }
  public void componentMoved(final ComponentEvent e) { }

  public void mouseWheelMoved(final MouseWheelEvent e) { }
}
