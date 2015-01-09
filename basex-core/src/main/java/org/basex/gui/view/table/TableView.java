package org.basex.gui.view.table;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This view creates a flat table view on the database contents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class TableView extends View implements Runnable {
  /** Zoom table. */
  private static final double[] ZOOM = {
    1, .99, .98, .97, 1, 1.03, 1.05, .9, .8, .6, .35, .18, .13, .09, .05, .03
  };
  /** Table data. */
  final TableData tdata;

  /** Table header. */
  private final TableHeader header;
  /** Table content area. */
  private final TableContent content;
  /** Table scrollbar. */
  final BaseXScrollBar scroll;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TableView(final ViewNotifier man) {
    super(TABLEVIEW, man);
    tdata = new TableData(gui.context, gui.gopts);
    layout(new BorderLayout());
    header = new TableHeader(this);
    add(header, BorderLayout.NORTH);
    scroll = new BaseXScrollBar(this);
    content = new TableContent(tdata, scroll);
    add(content, BorderLayout.CENTER);
    new BaseXPopup(this, POPUP);
  }

  @Override
  public void refreshInit() {
    tdata.rootRows = null;
    tdata.rows = null;

    final Data data = gui.context.data();
    if(!visible() || data == null) return;
    tdata.init(data);
    refreshContext(true, false);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    if(tdata.cols.length == 0) return;

    tdata.context(false);
    scroll.pos(0);
    if(tdata.rows == null) return;

    if(quick) {
      scroll.height(tdata.rows.size() * tdata.rowH(1));
      focus();
      repaint();
    } else {
      if(!more) tdata.resetFilter();
      gui.updating = true;
      new Thread(this).start();
    }
  }

  @Override
  public void refreshFocus() {
    if(!visible() || tdata.rows == null) return;
    repaint();
  }

  @Override
  public void refreshMark() {
    if(!visible() || tdata.rows == null) return;

    final Context context = gui.context;
    final DBNodes marked = context.marked;
    if(marked.size() != 0) {
      final int p = tdata.getRoot(context.data(), marked.pres[0]);
      if(p != -1) setPos(p);
    }
    repaint();
  }

  @Override
  public void refreshLayout() {
    if(!visible() || tdata.rows == null) return;

    scroll.height(tdata.rows.size() * tdata.rowH(1));
    scroll.refreshLayout();
    header.refreshLayout();
    refreshContext(false, true);
  }

  @Override
  public void refreshUpdate() {
    tdata.rootRows = null;
    tdata.init(gui.context.data());
    refreshContext(false, true);
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWTABLE);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWTABLE, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(tdata.rows == null && visible()) refreshInit();
  }

  @Override
  public void run() {
    /* Current zoom step. */
    int zoomstep = ZOOM.length;
    while(--zoomstep >= 0) {
      scroll.height(tdata.rows.size() * tdata.rowH(ZOOM[zoomstep]));
      repaint();
      Performance.sleep(25);
    }
    gui.updating = false;
    focus();
  }

  /**
   * Sets scrollbar position for the specified pre value.
   * @param pre pre value
   */
  private void setPos(final int pre) {
    final int off = getOff(pre);
    if(off == -1) return;
    final int h = getHeight() - header.getHeight() - 2 * tdata.rowH;
    final int y = (off - 1) * tdata.rowH;
    final int s = scroll.pos();
    if(y < s || y > s + h) scroll.pos(y);
  }

  /**
   * Returns list offset for specified pre value.
   * @param pre pre value
   * @return offset
   */
  private int getOff(final int pre) {
    final int ns = tdata.rows.size();
    for(int n = 0; n < ns; ++n) {
      if(tdata.rows.get(n) == pre) return n;
    }
    return -1;
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    super.mouseMoved(e);
    if(!visible() || tdata.rows == null) return;

    tdata.mouseX = e.getX();
    tdata.mouseY = e.getY();
    focus();
  }

  /**
   * Finds the current focus.
   */
  private void focus() {
    final int y = tdata.mouseY - header.getHeight() + scroll.pos();
    final int l = y / tdata.rowH;
    final boolean valid = y >= 0 && l < tdata.rows.size();

    final Data data = gui.context.data();
    int focused = -1;
    if(valid) {
      final int pre = tdata.rows.get(l);
      final TableIterator it = new TableIterator(data, tdata);
      final int c = tdata.column(getWidth() - scroll.getWidth(), tdata.mouseX);
      it.init(pre);
      while(it.more()) {
        if(it.col == c) {
          focused = it.pre;
          break;
        }
      }
    }
    gui.notify.focus(focused, this);
    content.repaint();

    final String str = content.focusedString;
    gui.cursor(valid && str != null && str.length() <= data.meta.maxlen ?
      CURSORHAND : CURSORARROW);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    gui.cursor(CURSORARROW);
    gui.notify.focus(-1, null);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final int pre = gui.context.focused;
    if(pre == -1) return;

    super.mousePressed(e);
    final Context context = gui.context;
    final Data data = gui.context.data();

    if(tdata.rows == null) return;

    if(e.getY() < header.getHeight()) return;

    if(SwingUtilities.isLeftMouseButton(e)) {
      if(e.getClickCount() == 1) {
        final int c = tdata.column(getWidth() - scroll.getWidth(), e.getX());
        final String str = content.focusedString;
        if(str == null || str.length() > data.meta.maxlen) return;
        if(!e.isShiftDown()) tdata.resetFilter();
        tdata.cols[c].filter = str;
        query();
        //repaint();
      } else {
        DBNodes nodes = context.marked;
        if(getCursor() == CURSORARROW) {
          nodes = new DBNodes(nodes.data, tdata.getRoot(nodes.data, pre));
        }
        gui.notify.context(nodes, false, null);
      }
    } else {
      final TableIterator it = new TableIterator(data, tdata);
      final int c = tdata.column(getWidth() - scroll.getWidth(), e.getX());
      it.init(pre);
      while(it.more()) {
        if(it.col == c) {
          gui.notify.mark(new DBNodes(data, it.pre), null);
          return;
        }
      }
    }
  }

  /**
   * Performs a table query.
   */
  void query() {
    final String query = tdata.find();
    if(query != null) gui.xquery(query, false);
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(tdata.rows == null) return;

    scroll.pos(scroll.pos() + e.getUnitsToScroll() * tdata.rowH);
    mouseMoved(e);
    repaint();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(tdata.rows == null) return;

    final int lines = (getHeight() - header.getHeight()) / tdata.rowH;
    final int oldPre = tdata.getRoot(gui.context.data(), gui.context.focused);
    int pre = oldPre;

    final IntList rows = tdata.rows;
    if(LINESTART.is(e)) {
      pre = rows.get(0);
    } else if(LINEEND.is(e)) {
      pre = rows.get(rows.size() - 1);
    } else if(PREVLINE.is(e)) {
      pre = rows.get(Math.max(0, getOff(pre) - 1));
    } else if(NEXTLINE.is(e)) {
      pre = rows.get(Math.min(rows.size() - 1, getOff(pre) + 1));
    } else if(PREVPAGE.is(e)) {
      pre = rows.get(Math.max(0, getOff(pre) - lines));
    } else if(NEXTPAGE.is(e)) {
      pre = rows.get(Math.min(rows.size() - 1, getOff(pre) + lines));
    }

    if(pre != oldPre) {
      setPos(pre);
      gui.notify.focus(pre, null);
    }
  }
}
