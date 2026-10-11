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
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * This view creates a flat table view on the database contents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TableView extends View {
  /** Zoom table. */
  private static final double[] ZOOM = {
    1, 0.99, 0.98, 0.97, 1, 1.03, 1.05, 0.9, 0.8, 0.6, 0.35, 0.18, 0.13, 0.09, 0.05, 0.03
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
   * @param notifier view notifier
   */
  public TableView(final ViewNotifier notifier) {
    super(TABLEVIEW, notifier, GUIOptions.SHOWTABLE, true);
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
      scroll.extent(tdata.rows.size() * tdata.rowH(1));
      focus();
      repaint();
    } else {
      if(!more) tdata.resetFilter();
      gui.updating = true;
      // current zoom step
      final int[] zoomstep = { ZOOM.length };
      final Timer timer = new Timer(25, e -> {
        if(--zoomstep[0] >= 0) {
          scroll.extent(tdata.rows.size() * tdata.rowH(ZOOM[zoomstep[0]]));
          repaint();
        } else {
          ((Timer) e.getSource()).stop();
          gui.updating = false;
          focus();
        }
      });
      timer.setInitialDelay(0);
      timer.start();
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
    if(!marked.isEmpty()) {
      final int p = tdata.getRoot(context.data(), marked.pre(0));
      if(p != -1) setPos(p);
    }
    repaint();
  }

  @Override
  public void refreshLayout() {
    if(!visible() || tdata.rows == null) return;

    scroll.extent(tdata.rows.size() * tdata.rowH(1));
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
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(tdata.rows == null && visible()) refreshInit();
  }

  /**
   * Sets scrollbar position for the specified PRE value.
   * @param pre PRE value
   */
  private void setPos(final int pre) {
    final int o = getOff(pre);
    if(o == -1) return;
    final int h = getHeight() - header.getHeight() - 2 * tdata.rowH;
    final int y = (o - 1) * tdata.rowH;
    final int s = scroll.pos();
    if(y < s || y > s + h) scroll.pos(y);
  }

  /**
   * Returns list offset for specified PRE value.
   * @param pre PRE value
   * @return offset
   */
  private int getOff(final int pre) {
    final int ns = tdata.rows.size();
    for(int n = 0; n < ns; n++) {
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
    gui.notify.focus(valid ? cell(data, tdata.rows.get(l), tdata.mouseX) : -1, this);
    content.repaint();

    final String str = content.focusedString;
    setCursor(valid && str != null && str.length() <= data.meta.maxlen ? CURSORHAND : CURSORARROW);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    gui.notify.focus(-1, null);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final int pre = gui.context.focused;
    if(pre == -1) return;

    super.mousePressed(e);
    final Context context = gui.context;
    final Data data = gui.context.data();

    if(tdata.rows == null || e.getY() < header.getHeight()) return;

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
          nodes = new DBNodes(nodes.data(), tdata.getRoot(nodes.data(), pre));
        }
        gui.notify.context(nodes, false, null);
      }
    } else {
      final int cell = cell(data, pre, e.getX());
      if(cell != -1) gui.notify.mark(new DBNodes(data, cell), null);
    }
  }

  /**
   * Returns the PRE value of the table cell at the given horizontal position.
   * @param data data reference
   * @param pre PRE value of the table row
   * @param x horizontal position
   * @return PRE value, or {@code -1}
   */
  private int cell(final Data data, final int pre, final int x) {
    final TableIterator iter = new TableIterator(data, tdata);
    final int c = tdata.column(getWidth() - scroll.getWidth(), x);
    iter.init(pre);
    while(iter.more()) {
      if(iter.col == c) return iter.pre;
    }
    return -1;
  }

  /**
   * Performs a table query.
   */
  void query() {
    final String query = tdata.find();
    if(query != null) gui.simpleQuery(query);
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
      pre = rows.peek();
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
