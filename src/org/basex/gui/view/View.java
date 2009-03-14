package org.basex.gui.view;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXPanel;

/**
 * View observer pattern. All inheriting classes are attached to the
 * views array
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class View extends BaseXPanel {
  /**
   * Registers the specified view.
   * @param man view manager
   * @param hlp help text
   */
  protected View(final ViewNotifier man, final byte[] hlp) {
    super(man.gui, hlp);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
    addComponentListener(this);
    setMode(GUIConstants.Fill.DOWN);
    setFocusable(true);
    man.add(this);
  }

  /**
   * Called when the data reference has changed.
   */
  protected abstract void refreshInit();

  /**
   * Called when a new focus has been chosen.
   */
  protected abstract void refreshFocus();

  /**
   * Called when a context set has been
   * marked.
   */
  protected abstract void refreshMark();

  /**
   * Called when a new context set has been chosen.
   * @param more show more details
   * @param quick perform a quick context switch
   */
  protected abstract void refreshContext(boolean more, boolean quick);

  /**
   * Called when GUI design has changed.
   */
  protected abstract void refreshLayout();

  /**
   * Called when updates have been done in the data structure.
   */
  protected abstract void refreshUpdate();

  @Override
  public void mouseEntered(final MouseEvent e) {
    if(!gui.updating && GUIProp.mousefocus) requestFocusInWindow();
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if(!gui.updating) gui.cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(!gui.updating) requestFocusInWindow();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(gui.updating) return;
    final boolean ctrl = e.isControlDown();
    final boolean shift = e.isShiftDown();
    final int key = e.getKeyCode();

    if(key == KeyEvent.VK_ESCAPE) {
      gui.fullscreen(false);
    } else if(key == KeyEvent.VK_ENTER) {
      GUICommands.FILTER.execute(gui);
    } else if(key == KeyEvent.VK_SPACE) {
      gui.notify.mark(ctrl ? 2 : shift ? 1 : 0, null);
    } else if(key == KeyEvent.VK_BACK_SPACE) {
      GUICommands.GOBACK.execute(gui);
    }
  }

  @Override
  public final String toString() {
    return getName();
  }
}
