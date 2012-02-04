package org.basex.gui.view.table;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.gui.view.table.TableData.TableCol;

/**
 * This is the header of the table view.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class TableHeader extends BaseXPanel {
  /** View reference. */
  final TableView view;
  /** Table Data. */
  final TableData tdata;
  /** Temporary Input Box. */
  TableInput box;
  /** Current input column. */
  int inputCol = -1;

  /** Header flag. */
  private boolean header;
  /** Clicked column. */
  private int clickCol = -1;
  /** Moved column. */
  private int moveC = -1;
  /** Moved X position. */
  private int mouseX = -1;

  /**
   * Constructor.
   * @param v view reference
   */
  TableHeader(final TableView v) {
    super(v.gui);
    mode(Fill.NONE).setFocusable(true);
    tdata = v.tdata;
    view = v;
    BaseXLayout.setHeight(this, gui.gprop.num(GUIProp.FONTSIZE) + 8 << 1);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    // restore default focus traversal with TAB key
    setFocusTraversalKeysEnabled(false);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        filter(e.getOppositeComponent() instanceof TableView ? 0 :
          tdata.cols.length - 1);
      }
      @Override
      public void focusLost(final FocusEvent e) {
        // tab key pressed..
        if(box != null) box.stop();
        inputCol = -1;
        repaint();
      }
    });
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    g.setFont(font);
    g.setColor(Color.black);
    if(tdata.rows == null) {
      BaseXLayout.drawCenter(g, NO_DATA, getWidth(), getHeight() / 2);
      return;
    }

    final int fsz = gui.gprop.num(GUIProp.FONTSIZE);
    final int bs = BaseXBar.SIZE;
    int w = getWidth();
    final int h = getHeight();
    final int hh = h >> 1;
    g.setColor(color2);
    g.drawLine(0, h - 1, w, h - 1);

    w -= bs;
    double x = 0;
    final int nc = tdata.cols.length;
    for(int n = 0; n < nc; ++n) {
      final double cw = w * tdata.cols[n].width;
      final double ce = x + cw;

      // header
      final boolean clicked = n == clickCol && moveC == -1 && header;
      BaseXLayout.drawCell(g, (int) x, (int) ce + 1, 0, hh, clicked);
      // input field
      g.setColor(Color.white);
      g.fillRect((int) x + 1, hh, (int) ce - (int) x - 2, hh - 2);
      g.drawLine((int) ce - 1, hh - 1, (int) ce - 1, h - 2);
      g.setColor(GRAY);
      g.drawLine((int) ce, hh - 1, (int) ce, h - 2);

      // draw headers
      g.setColor(Color.black);
      g.setFont(bfont);

      final int off = clicked ? 1 : 0;
      BaseXLayout.chopString(g, tdata.cols[n].name,
          (int) x + 4 + off, 2 + off, (int) cw, fsz);

      if(n == tdata.sortCol) {
        if(tdata.asc) g.fillPolygon(new int[] { (int) ce - 9 + off,
            (int) ce - 3 + off, (int) ce - 6 + off },
            new int[] { 4 + off, 4 + off, 8 + off }, 3);
        else g.fillPolygon(new int[] { (int) ce - 9 + off, (int) ce - 3 + off,
            (int) ce - 6 + off }, new int[] { 8 + off, 8 + off, 4 + off }, 3);
      }

      // draw filter texts
      if(box != null && inputCol == n) {
        box.paint(g, (int) x, hh, (int) ce - (int) x, hh);
      } else {
        g.setColor(Color.black);
        g.setFont(font);
        g.drawString(tdata.cols[n].filter, (int) x + 5, h - 7);
      }
      x = ce;
    }

    final boolean clicked = nc == clickCol;
    BaseXLayout.drawCell(g, (int) x, w + bs, 0, hh, clicked && header);
    BaseXLayout.drawCell(g, (int) x, w + bs, hh - 1, h, clicked && !header);
    g.setColor(Color.black);
    g.setFont(bfont);
    smooth(g);

    int o = header && clicked ? 1 : 0;
    g.fillPolygon(new int[] { (int) x + o + 4, (int) x + o + bs - 4,
        (int) x + o + bs / 2 }, new int[] { o + 6, o + 6, o + bs - 3 }, 3);

    o = !header && clicked ? 1 : 0;
    final byte[] reset = { 'x' };
    x += (bs - BaseXLayout.width(g, reset)) / 2d;
    BaseXLayout.chopString(g, reset, (int) x + o, hh + o + 1, w, fsz);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(tdata.rows == null) return;

    Cursor cursor = CURSORARROW;
    mouseX = e.getX();

    final int w = getWidth() - BaseXBar.SIZE;
    if(header(e.getY())) {
      moveC = colSep(w, mouseX);
      if(moveC != -1) cursor = CURSORMOVEH;
    } else {
      moveC = -1;
      if(mouseX < w) cursor = CURSORTEXT;
      if(gui.gprop.is(GUIProp.MOUSEFOCUS)) {
        final int c = tdata.column(w, mouseX);
        if(c != -1) filter(c);
      }
    }
    view.gui.cursor(cursor);
  }

  /**
   * Returns the column separator at the specified horizontal position.
   * @param w panel width
   * @param mx mouse position
   * @return column
   */
  private int colSep(final int w, final int mx) {
    double x = 0;
    for(int i = 0; i < tdata.cols.length; ++i) {
      if(i > 0 && Math.abs(mx - x) < 3) return i;
      x += w * tdata.cols[i].width;
    }
    return -1;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(tdata.rows == null) return;

    if(moveC != -1) {
      final int x = e.getX();
      final double p = (double) (x - mouseX) / (getWidth() - BaseXBar.SIZE);
      final double[] ww = new double[tdata.cols.length];
      for(int w = 0; w < ww.length; ++w) ww[w] = tdata.cols[w].width;

      if(e.isShiftDown()) {
        ww[moveC - 1] += p;
        ww[moveC] -= p;
      } else {
        for(int i = 0; i < moveC; ++i) ww[i] += p / moveC;
        for(int i = moveC; i < ww.length; ++i) ww[i] -= p / (ww.length - moveC);
      }
      for(final double w : ww) if(w < 0.0001) return;
      mouseX = x;

      for(int w = 0; w < ww.length; ++w) tdata.cols[w].width = ww[w];
    } else if(clickCol != -1) {
      int c = tdata.column(getWidth() - BaseXBar.SIZE, e.getX());
      if(c == -1) c = tdata.cols.length;
      if(c != clickCol || header != header(e.getY())) clickCol = -1;
    }
    view.repaint();
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if(tdata.rows == null) return;

    view.gui.cursor(CURSORARROW);
    clickCol = -1;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(tdata.rows == null || !SwingUtilities.isLeftMouseButton(e)) return;

    clickCol = tdata.column(getWidth() - BaseXBar.SIZE, mouseX);
    if(clickCol == -1) clickCol = tdata.cols.length;
    header = header(e.getY());
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(tdata.rows == null) return;

    if(!SwingUtilities.isLeftMouseButton(e)) {
      chooseCols(e);
    } else {
      if(clickCol == -1) return;
      // header
      if(header(e.getY())) {
        if(moveC == -1) {
          if(clickCol == tdata.cols.length) {
            chooseRoot(e);
          } else {
            // sort data in current column
            view.gui.cursor(CURSORWAIT);
            tdata.asc = tdata.sortCol != clickCol || !tdata.asc;
            tdata.sortCol = clickCol;
            tdata.sort();
            view.gui.cursor(CURSORARROW, true);
          }
        }
      } else {
        // activate table filter
        if(clickCol != tdata.cols.length) {
          filter(clickCol);
        } else {
          // reset table filter
          tdata.resetFilter();
          view.query();
        }
      }
    }
    clickCol = -1;
    view.repaint();
  }

  /**
   * Shows a popup menu to choose main category to be displayed.
   * @param e event reference
   */
  private void chooseRoot(final MouseEvent e) {
    if(tdata.roots.size() == 0) return;

    final Data data = view.gui.context.data();
    final JPopupMenu popup = new JPopupMenu();
    final byte[] root = data.tagindex.key(tdata.root);
    for(final byte[] en : tdata.roots) {
      final int id = data.tagindex.id(en);
      final JMenuItem mi = new JRadioButtonMenuItem(string(en), eq(root, en));
      mi.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent ac) {
          tdata.init(data, id);
          view.refreshContext(true, false);
        }
      });
      popup.add(mi);
    }
    popup.show(this, e.getX(), e.getY());
  }

  /**
   * Shows a popup menu to filter the visible columns.
   * @param e event reference
   */
  private void chooseCols(final MouseEvent e) {
    final JPopupMenu popup = new JPopupMenu();
    for(final TableCol col : tdata.cols) {
      final String item = (col.elem ? "" : "@") + string(col.name);
      final JMenuItem mi = new JCheckBoxMenuItem(item, col.width != 0);
      mi.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent ac) {
          final boolean sel = mi.isSelected();
          boolean vis = sel;
          // disallow removal of last visible column
          for(final TableCol c : tdata.cols) vis |= c != col && c.width != 0;

          if(vis) {
            col.hwidth = sel ? 0 : col.width;
            col.width = sel ? col.hwidth : 0;
          } else {
            mi.setSelected(true);
          }

          popup.setVisible(true);
          tdata.setWidths(true);
          view.refreshContext(true, true);
        }
      });
      popup.add(mi);
    }
    popup.show(this, e.getX(), e.getY());
  }

  /**
   * Checks if specified y value lies in table header.
   * @param y position
   * @return true for table header, false for input field
   */
  private boolean header(final int y) {
    return y < getHeight() >> 1;
  }

  /**
   * Handles the filter columns.
   * @param col current column
   */
  void filter(final int col) {
    // activate table filter
    if(inputCol != col) {
      if(box != null) box.stop();
      box = new TableInput(this, tdata.cols[col].filter);
      inputCol = col;
    }
    requestFocusInWindow();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(tdata.roots.size() == 0 || box == null || control(e) || inputCol == -1)
      return;

    if(ENTER.is(e)) {
      box.stop();
      inputCol = -1;
      final Nodes marked = view.gui.context.marked;
      if(marked.size() != 0) view.gui.notify.context(marked, false, null);
    } else if(TAB.is(e)) {
      tdata.cols[inputCol].filter = box.text;
      box.stop();
      final int in = inputCol + (e.isShiftDown() ? -1 : 1);
      if(in < 0) {
        transferFocusBackward();
      } else if(in == tdata.cols.length) {
        transferFocus();
      } else {
        inputCol = in;
        box = new TableInput(TableHeader.this, tdata.cols[inputCol].filter);
      }
    } else {
      box.code(e);
    }
    repaint();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(tdata.roots.size() == 0 || box == null || inputCol == -1 ||
        control(e) || !box.add(e)) return;
    tdata.cols[inputCol].filter = box.text;
    view.query();
  }
}

