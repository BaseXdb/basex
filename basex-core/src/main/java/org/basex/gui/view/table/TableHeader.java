package org.basex.gui.view.table;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.table.TableData.TableCol;

/**
 * This is the header of the table view.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class TableHeader extends BaseXPanel {
  /** View reference. */
  private final TableView view;
  /** Table Data. */
  private final TableData tdata;
  /** Temporary Input Box. */
  private TableInput box;
  /** Current input column. */
  private int inputCol = -1;

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
    setOpaque(false);
    setFocusable(true);
    tdata = v.tdata;
    view = v;
    refreshLayout();
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

  /**
   * Called when GUI design has changed.
   */
  public void refreshLayout() {
    setPreferredSize(new Dimension(getPreferredSize().width, fontSize * 3));
    setSize(getPreferredSize());
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    g.setFont(font);
    g.setColor(GUIConstants.FORE);
    if(tdata.rows == null) {
      BaseXLayout.drawCenter(g, NO_DATA, getWidth(), getHeight() / 2);
      return;
    }

    final int fsz = fontSize;
    int w = getWidth();
    final int h = getHeight();
    final int hh = h / 2;
    g.setColor(color2);
    g.drawLine(0, h - 1, w, h - 1);

    final int bs = BaseXScrollBar.SIZE;
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
      g.setColor(GUIConstants.BACK);
      g.fillRect((int) x + 1, hh, (int) ce - (int) x - 2, hh - 2);
      g.drawLine((int) ce - 1, hh - 1, (int) ce - 1, h - 2);
      g.setColor(gray);
      g.drawLine((int) ce, hh - 1, (int) ce, h - 2);

      // draw headers
      g.setColor(GUIConstants.FORE);
      g.setFont(bfont);

      final int off = clicked ? 1 : 0;
      BaseXLayout.chopString(g, tdata.cols[n].name, (int) x + 4 + off, 2 + off, (int) cw, fsz);

      if(n == tdata.sortCol) {
        if(tdata.asc) g.fillPolygon(new int[] { (int) ce - 9 + off, (int) ce - 3 + off,
            (int) ce - 6 + off },
            new int[] { 4 + off, 4 + off, 8 + off }, 3);
        else g.fillPolygon(new int[] { (int) ce - 9 + off, (int) ce - 3 + off,
            (int) ce - 6 + off }, new int[] { 8 + off, 8 + off, 4 + off }, 3);
      }

      // draw filter texts
      if(box != null && inputCol == n) {
        box.paint(g, (int) x, hh, (int) ce - (int) x, hh);
      } else {
        g.setColor(GUIConstants.FORE);
        g.setFont(font);
        g.drawString(tdata.cols[n].filter, (int) x + 5, h - 7);
      }
      x = ce;
    }

    final boolean clicked = nc == clickCol;
    BaseXLayout.drawCell(g, (int) x, w + bs, 0, hh, clicked && header);
    BaseXLayout.drawCell(g, (int) x, w + bs, hh - 1, h, clicked && !header);
    BaseXLayout.antiAlias(g);

    int o = header && clicked ? 1 : 0;
    final int xo = (int) (4 * SCALE), yo = (int) (6 * SCALE);
    g.setColor(GUIConstants.FORE);
    g.fillPolygon(
        new int[] { (int) x + o + xo, (int) x + o + bs - xo, (int) x + o + bs / 2 },
        new int[] { o + yo, o + yo, o + bs - yo }, 3);
    g.setFont(bfont);

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

    final int w = getWidth() - BaseXScrollBar.SIZE;
    if(header(e.getY())) {
      moveC = colSep(w, mouseX);
      if(moveC != -1) cursor = CURSORMOVEH;
    } else {
      moveC = -1;
      if(mouseX < w) cursor = CURSORTEXT;
      if(gui.gopts.get(GUIOptions.MOUSEFOCUS)) {
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
    final TableCol[] cols = tdata.cols;
    final int tl = cols.length;
    for(int i = 0; i < tl; ++i) {
      if(i > 0 && Math.abs(mx - x) < 3) return i;
      x += w * cols[i].width;
    }
    return -1;
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(tdata.rows == null) return;

    if(moveC != -1) {
      final int x = e.getX();
      final double p = (double) (x - mouseX) / (getWidth() - BaseXScrollBar.SIZE);
      final double[] ww = new double[tdata.cols.length];
      final int wl = ww.length;
      for(int w = 0; w < wl; ++w) ww[w] = tdata.cols[w].width;

      if(e.isShiftDown()) {
        ww[moveC - 1] += p;
        ww[moveC] -= p;
      } else {
        for(int i = 0; i < moveC; ++i) ww[i] += p / moveC;
        for(int i = moveC; i < wl; ++i) ww[i] -= p / (wl - moveC);
      }
      for(final double w : ww) if(w < 0.0001) return;
      mouseX = x;

      for(int w = 0; w < wl; ++w) tdata.cols[w].width = ww[w];
    } else if(clickCol != -1) {
      int c = tdata.column(getWidth() - BaseXScrollBar.SIZE, e.getX());
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

    clickCol = tdata.column(getWidth() - BaseXScrollBar.SIZE, mouseX);
    if(clickCol == -1) clickCol = tdata.cols.length;
    header = header(e.getY());
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(tdata.rows == null) return;

    if(SwingUtilities.isLeftMouseButton(e)) {
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
        if(clickCol == tdata.cols.length) {
          // reset table filter
          tdata.resetFilter();
          view.query();
        } else {
          filter(clickCol);
        }
      }
    } else {
      chooseCols(e);
    }
    clickCol = -1;
    view.repaint();
  }

  /**
   * Shows a popup menu to choose main category to be displayed.
   * @param e event reference
   */
  private void chooseRoot(final MouseEvent e) {
    if(tdata.roots.isEmpty()) return;

    final Data data = view.gui.context.data();
    final JPopupMenu popup = new JPopupMenu();
    final byte[] root = data.elemNames.key(tdata.root);
    for(final byte[] en : tdata.roots) {
      final int id = data.elemNames.id(en);
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
  private void filter(final int col) {
    // activate table filter
    if(inputCol != col && tdata.cols.length != 0) {
      if(box != null) box.stop();
      box = new TableInput(this, tdata.cols[col].filter);
      inputCol = col;
    }
    requestFocusInWindow();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(tdata.roots.isEmpty() || box == null || control(e) || inputCol == -1) return;

    if(ENTER.is(e)) {
      box.stop();
      inputCol = -1;
      final DBNodes marked = view.gui.context.marked;
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
        box = new TableInput(this, tdata.cols[inputCol].filter);
      }
    } else {
      box.code(e);
    }
    repaint();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(tdata.roots.isEmpty() || box == null || inputCol == -1 ||
        control(e) || !box.add(e)) return;
    tdata.cols[inputCol].filter = box.text;
    view.query();
  }
}

