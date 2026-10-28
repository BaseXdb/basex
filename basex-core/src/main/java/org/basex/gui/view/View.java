package org.basex.gui.view;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.event.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * View observer pattern. All inheriting classes are attached to the
 * views array
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class View extends BaseXPanel {
  /** Visibility option. */
  private final BooleanOption show;
  /** Indicates if this view relies on a database instance. */
  private final boolean db;

  /**
   * Registers the specified view.
   * @param name     name of view
   * @param notifier view notifier
   * @param show     visibility option
   * @param db       indicates if this view relies on a database instance
   */
  protected View(final String name, final ViewNotifier notifier, final BooleanOption show,
      final boolean db) {
    super(notifier.gui);
    this.show = show;
    this.db = db;
    setFocusable(true);
    setBackground(GUIConstants.backColor);
    setName(name);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addKeyListener(this);
    addComponentListener(this);
    notifier.add(this);
  }

  /**
   * Called when the data reference has changed.
   */
  public void refreshInit() { }

  /**
   * Called when a new focus has been chosen.
   */
  public void refreshFocus() { }

  /**
   * Called when a context set has been
   * marked.
   */
  public void refreshMark() { }

  /**
   * Called when a new context set has been chosen.
   * @param more show more details
   * @param quick perform a quick context switch
   */
  @SuppressWarnings("unused")
  public void refreshContext(final boolean more, final boolean quick) { }

  /**
   * Called when GUI design has changed.
   */
  public abstract void refreshLayout();

  /**
   * Called when updates have been done in the data structure.
   */
  public void refreshUpdate() { }

  /**
   * Tests if this view is currently marked as visible.
   * @return result of check
   */
  public boolean visible() {
    return gui.gopts.get(show);
  }

  /**
   * Sets a flag denoting the visibility of the view.
   * @param v visibility flag
   */
  public final void visible(final boolean v) {
    gui.gopts.set(show, v);
  }

  /**
   * Tests if this view relies on a database instance.
   * @return result of check
   */
  final boolean db() {
    return db;
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    if(!gui.updating && gui.gopts.get(GUIOptions.MOUSEFOCUS)) requestFocusInWindow();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(!gui.updating) requestFocusInWindow();
  }

  /**
   * Adds the focused node to the marked nodes (Shift) or toggles it (shortcut key).
   * @param e mouse event
   * @return {@code true} if the marked nodes were changed
   */
  protected final boolean markFocused(final MouseEvent e) {
    return mark(e, gui.context.focused);
  }

  /**
   * Clears the marked nodes after a left click on empty space.
   * @param e mouse event
   */
  protected final void unmark(final MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e) && !gui.context.marked.isEmpty()) {
      gui.notify.mark(new DBNodes(gui.context.data()), null);
    }
  }

  /**
   * Adds the specified node to the marked nodes (Shift) or toggles it (shortcut key).
   * @param e mouse event
   * @param pre PRE value of the node
   * @return {@code true} if the marked nodes were changed
   */
  protected final boolean mark(final MouseEvent e, final int pre) {
    final int mode = e.isShiftDown() ? 1 : sc(e) && SwingUtilities.isLeftMouseButton(e) ? 2 : 0;
    if(mode != 0) gui.notify.mark(mode, pre);
    return mode != 0;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(gui.updating || gui.context.data() == null) return;
    if(SPACE.is(e)) {
      gui.notify.mark(sc(e) ? 2 : e.isShiftDown() ? 1 : 0);
    } else if(ENTER.is(e)) {
      GUIMenuCmd.C_FILTER_NODES.execute(gui);
    }
  }

  @Override
  public final String toString() {
    return getName();
  }
}
