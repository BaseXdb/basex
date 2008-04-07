package org.basex.gui.view.table;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewData;
import org.basex.query.fs.FSUtils;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This view creates a flat table view on the database contents.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableView extends View implements Runnable {
  /** Zoom table. */
  static final double[] ZOOM = {
    1, .99, .98, .97, 1, 1.03, 1.05, .9, .8, .6, .35, .18, .13, .09, .05, .03
  };
  /** Zoom table. */
  private int zoomstep;
  /** Table data. */
  private TableData tdata = new TableData();

  /** Scrollbar reference. */
  private TableHeader header;
  /** Scrollbar reference. */
  private TableContent content;
  /** Scrollbar reference. */
  private BaseXBar scroll;

  /**
   * Default constructor.
   * @param help help text
   */
  public TableView(final byte[] help) {
    super(help);
    setLayout(new BorderLayout());
    header = new TableHeader(tdata, this);
    add(header, BorderLayout.NORTH);
    scroll = new BaseXBar(this);
    content = new TableContent(tdata, scroll);
    add(content, BorderLayout.CENTER);
    popup = new BaseXPopup(this, GUIConstants.POPUP);
  }

  @Override
  public void refreshInit() {
    scroll.pos(0);
    scroll.height(0);
    tdata.rows = null;
    tdata.invalid = true;

    final Context context = GUI.context;
    if(!context.db() || !GUIProp.showtable) return;
    tdata.init(context.data());
    refreshContext(true, false);
  }
  
  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    if(tdata.invalid) return;
    if(!GUIProp.showtable) {
      tdata.invalid = true;
      tdata.rows = null;
      return;
    }
    
    tdata.context();
    scroll.pos(0);
    if(quick) {
      scroll.height(tdata.rows.size * tdata.rowH(1));
      findFocus();
      repaint();
    } else {
      working = true;
      new Thread(this).start();
    }
  }
  
  @Override
  public void refreshFocus() {
    if(tdata.invalid) return;
    repaint();
  }

  @Override
  public void refreshMark() {
    if(tdata.invalid) return;

    final Context context = GUI.context;
    final Nodes marked = context.marked();
    if(marked.size != 0) {
      final int p = tdata.getRoot(context.data(), marked.pre[0]);
      if(p != -1) setPos(p);
    }
    repaint();
  }

  @Override
  public void refreshLayout() {
    if(tdata.invalid) return;
    scroll.height(tdata.rows.size * tdata.rowH(1));
    refreshUpdate();
  }
  
  @Override
  public void refreshUpdate() {
    if(tdata.invalid || !GUIProp.showtable) return;
    tdata.init(GUI.context.data());
    refreshContext(false, true);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if(tdata.rows == null && GUIProp.showtable) {
      refreshInit();
      return;
    }
  }

  /**
   * Starts a context switch animation.
   */
  public void run() {
    zoomstep = ZOOM.length;
    while(--zoomstep >= 0) {
      scroll.height(tdata.rows.size * tdata.rowH(ZOOM[zoomstep]));
      repaint();
      Performance.sleep(25);
    }
    working = false;
    findFocus();
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
    for(int n = 0; n < tdata.rows.size; n++) {
      if(tdata.rows.get(n) == pre) return n;
    }
    return -1;
  }
  
  @Override
  public void mouseMoved(final MouseEvent e) {
    super.mouseMoved(e);
    if(tdata.invalid) return;

    tdata.mouseX = e.getX();
    tdata.mouseY = e.getY();
    findFocus();
  }
  
  /**
   * Finds the current focus.
   */
  private void findFocus() {
    final int y = tdata.mouseY - header.getHeight() + scroll.pos();
    final int l = y / tdata.rowH;
    final boolean valid = y >= 0 && l < tdata.rows.size;
    
    if(valid) {
      final int pre = tdata.rows.get(l);
      final Context context = GUI.context;
      TableIterator it = new TableIterator(context.data(), tdata);
      final int c = tdata.column(getWidth() - BaseXBar.SIZE, tdata.mouseX);
      it.init(pre);
      while(it.more()) {
        if(it.col == c) {
          View.notifyFocus(it.pre, this);
          content.repaint();
        }
      }
    }
    final String str = content.focusedString;
    final Data data = GUI.context.data();
    GUI.get().cursor(valid && (str != null &&
        str.length() <= Token.MAXLEN || data.deepfs && tdata.mouseX < 20) ?
        GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    GUI.get().cursor(GUIConstants.CURSORARROW);
    View.notifyFocus(-1, null);
  }
  
  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    final Data data = GUI.context.data();
    if(tdata.invalid || data.deepfs && tdata.mouseX < 20) return;
    
    if(e.getY() < header.getHeight()) return;
    
    final int pre = focused;
    if(SwingUtilities.isLeftMouseButton(e)) {
      if(e.getClickCount() == 1) {
        final String str = content.focusedString;
        if(str == null || str.length() > Token.MAXLEN) return;
        if(!e.isShiftDown()) tdata.resetFilter();
        tdata.filter[tdata.column(getWidth() - BaseXBar.SIZE, e.getX())] = str;
        tdata.find();
      } else {
        Nodes nodes = GUI.context.marked();
        if(getCursor() == GUIConstants.CURSORARROW) {
          nodes = new Nodes(tdata.getRoot(nodes.data, pre), nodes.data);
        }
        View.notifyContext(nodes, false);
      }
    } else {
      if(pre != -1) {
        final Context context = GUI.context;
        TableIterator it = new TableIterator(context.data(), tdata);
        final int c = tdata.column(getWidth() - BaseXBar.SIZE, e.getX());
        it.init(pre);
        while(it.more()) {
          if(it.col == c) {
            notifyMark(new Nodes(it.pre, context.data()));
            return;
          }
        }
      }
    }
  }
  
  @Override
  public void mouseClicked(final MouseEvent e) {
    final Data data = GUI.context.data();
    
    if(data.deepfs && tdata.mouseX < 20) {
      FSUtils.launch(data, ViewData.parent(data, focused));
    }
  }
  
  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(tdata.invalid) return;
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * tdata.rowH);
    mouseMoved(e);
    repaint();
  }
  
  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(tdata.invalid || e.isAltDown()) return;
    final int key = e.getKeyCode();
    
    final int lines = (getHeight() - header.getHeight()) / tdata.rowH;
    final int oldPre = tdata.getRoot(GUI.context.data(), focused);
    int pre = oldPre;
    
    final IntList rows = tdata.rows;
    if(key == KeyEvent.VK_HOME) {
      pre = rows.get(0);
    } else if(key == KeyEvent.VK_END) {
      pre = rows.get(rows.size - 1);
    } else if(key == KeyEvent.VK_UP) {
      pre = rows.get(Math.max(0, getOff(pre) - 1));
    } else if(key == KeyEvent.VK_DOWN) {
      pre = rows.get(Math.min(rows.size - 1, getOff(pre) + 1));
    } else if(key == KeyEvent.VK_PAGE_UP) {
      pre = rows.get(Math.max(0, getOff(pre) - lines));
    } else if(key == KeyEvent.VK_PAGE_DOWN) {
      pre = rows.get(Math.min(rows.size - 1, getOff(pre) + lines));
    }
    
    if(pre != oldPre) {
      setPos(pre);
      notifyFocus(pre, null);
    }
  }
}
