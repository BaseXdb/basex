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
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.view.View;

/**
 * Abstract panel implementation with a number of predefined listeners.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class BaseXPanel extends BaseXBack implements MouseListener,
    MouseMotionListener, ComponentListener, KeyListener, MouseWheelListener {
  
  /**
   * Constructor, setting the help text.
   * @param hlp help text
   */
  protected BaseXPanel(final byte[] hlp) {
    BaseXLayout.addHelp(this, hlp);
  }
  
  public void mouseEntered(final MouseEvent e) { }
  public void mousePressed(final MouseEvent e) { }
  public void mouseReleased(final MouseEvent e) { }
  public void mouseClicked(final MouseEvent e) { }
  public void mouseExited(final MouseEvent e) { }
  public void mouseMoved(final MouseEvent e) { }
  public void mouseDragged(final MouseEvent e) { }

  public void keyPressed(final KeyEvent e) {
    if(View.updating || !e.isAltDown()) return;
    
    final int key = e.getKeyCode();
    if(key == KeyEvent.VK_LEFT) {
      GUICommands.GOBACK.execute();
    } else if(key == KeyEvent.VK_RIGHT) {
      GUICommands.GOFORWARD.execute();
    } else if(key == KeyEvent.VK_UP) {
      GUICommands.GOUP.execute();
    } else if(key == KeyEvent.VK_HOME) {
      GUICommands.ROOT.execute();
    }
  }

  public void keyTyped(final KeyEvent e) {
    if(View.updating || !e.isControlDown()) return;
    final char key = e.getKeyChar();
    if(key == '+' || key == '-' || key == '=') {
      GUIProp.fontsize = Math.max(1, GUIProp.fontsize + (key == '-' ? -1 : 1));
      View.notifyLayout();
    } else if(key == '0') {
      GUIProp.fontsize = 12;
      View.notifyLayout();
    }
   
  }
  public void keyReleased(final KeyEvent e) { }

  public void componentResized(final ComponentEvent e) { }
  public void componentHidden(final ComponentEvent e) { }
  public void componentShown(final ComponentEvent e) { }
  public void componentMoved(final ComponentEvent e) { }

  public void mouseWheelMoved(final MouseWheelEvent e) { }
}
