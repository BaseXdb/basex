package org.basex.gui.view.table;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.gui.view.View;
import org.basex.gui.view.table.TableData.TableCol;

/**
 * This is the header of the table view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  /** Shift flag. */
  private boolean shift;
  /** Alt flag. */
  private boolean alt;

  /**
   * Constructor.
   * @param d table data
   * @param v view reference
   */
  TableHeader(final TableData d, final TableView v) {
    super(HELPTABLEHEAD);
    setMode(FILL.UP);
    tdata = d;
    view = v;
    BaseXLayout.setHeight(this, (GUIProp.fontsize + 8) << 1);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    // restore default focus traversal with TAB key
    setFocusTraversalKeysEnabled(false);

    addFocusListener(new FocusAdapter() {
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

    BaseXLayout.antiAlias(g);
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    g.setFont(GUIConstants.font);
    g.setColor(Color.black);
    if(tdata.rows == null) {
      BaseXLayout.drawCenter(g, NOTABLE, getWidth(), getHeight() / 2);
      return;
    }

    final int bs = BaseXBar.SIZE;
    int w = getWidth();
    final int h = getHeight();
    final int hh = h >> 1;
    g.setColor(GUIConstants.color1);
    g.drawLine(0, h - 1, w, h - 1);

    w -= bs;
    double x = 0;
    final int nc = tdata.cols.length;
    for(int n = 0; n < nc; n++) {
      final double cw = w * tdata.cols[n].width;
      final double ce = x + cw;

      // header
      final boolean clicked = n == clickCol && moveC == -1 && header;
      BaseXLayout.drawCell(g, (int) x, (int) ce + 1, 0, hh, clicked);
      // input field
      g.setColor(Color.white);
      g.fillRect((int) x + 1, hh, (int) ce - (int) x - 2, hh - 2);
      g.drawLine((int) ce - 1, hh - 1, (int) ce - 1, h - 2);
      g.setColor(GUIConstants.color5);
      g.drawLine((int) ce, hh - 1, (int) ce, h - 2);

      // draw headers
      g.setColor(Color.black);
      g.setFont(GUIConstants.bfont);

      final int off = clicked ? 1 : 0;
      BaseXLayout.chopString(g, tdata.cols[n].name,
          (int) x + 4 + off, 2 + off, (int) cw);

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
        g.setFont(GUIConstants.font);
        g.drawString(tdata.cols[n].filter, (int) x + 5, h - 7);
      }
      x = ce;
    }

    final boolean clicked =  nc == clickCol;
    BaseXLayout.drawCell(g, (int) x, w + bs, 0, hh, clicked && header);
    BaseXLayout.drawCell(g, (int) x, w + bs, hh - 1, h, clicked && !header);
    g.setColor(Color.black);
    g.setFont(GUIConstants.bfont);

    int o = header && clicked ? 1 : 0;
    g.fillPolygon(new int[] { (int) x + o + 3, (int) x + o + bs - 5,
        (int) x + o + bs / 2 - 1 }, new int[] { o + 6, o + 6, o + bs - 3 }, 3);

    o = !header && clicked ? 1 : 0;
    final byte[] reset = { 'x' };
    x += (bs - BaseXLayout.width(g, reset)) / 2;
    BaseXLayout.chopString(g, reset, (int) x + o, hh + o + 1, w);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(tdata.rows == null) return;

    Cursor cursor = GUIConstants.CURSORARROW;
    mouseX = e.getX();

    final int w = getWidth() - BaseXBar.SIZE;
    if(header(e.getY())) {
      moveC = colSep(w, mouseX);
      if(moveC != -1) cursor = GUIConstants.CURSORMOVEH;
    } else {
      moveC = -1;
      if(mouseX < w) cursor = GUIConstants.CURSORTEXT;
      if(GUIProp.mousefocus) {
        final int col = tdata.column(w, mouseX);
        if(col != -1) filter(col);
      }
    }
    GUI.get().cursor(cursor);
  }

  /**
   * Returns the column separator at the specified horizontal position.
   * @param w panel width
   * @param mx mouse position
   * @return column
   */
  int colSep(final int w, final int mx) {
    double x = 0;
    for(int i = 0; i < tdata.cols.length; i++) {
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
      for(int w = 0; w < ww.length; w++) ww[w] = tdata.cols[w].width;

      if(e.isShiftDown()) {
        ww[moveC - 1] += p;
        ww[moveC] -= p;
      } else {
        for(int i = 0; i < moveC; i++) ww[i] += p / moveC;
        for(int i = moveC; i < ww.length; i++) ww[i] -= p / (ww.length - moveC);
      }
      for(final double w : ww) if(w < 0.0001) return;
      mouseX = x;

      for(int w = 0; w < ww.length; w++) tdata.cols[w].width = ww[w];
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

    GUI.get().cursor(GUIConstants.CURSORARROW);
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
            GUI.get().cursor(GUIConstants.CURSORWAIT);
            tdata.asc = tdata.sortCol == clickCol ? !tdata.asc : true;
            tdata.sortCol = clickCol;
            tdata.sort();
            GUI.get().cursor(GUIConstants.CURSORARROW, true);
          }
        }
      } else {
        // activate table filter
        if(clickCol != tdata.cols.length) {
          filter(clickCol);
        } else {
          // reset table filter
          tdata.resetFilter();
          tdata.find();
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
    if(tdata.roots.size == 0) return;

    final Data data = GUI.context.data();
    final JPopupMenu popup = new JPopupMenu("Items");
    final byte[] root = data.tags.key(tdata.root);
    for(final byte[] en : tdata.roots) {
      final int id = data.tags.id(en);
      final JMenuItem mi = new JRadioButtonMenuItem(string(en), eq(root, en));
      mi.addActionListener(new ActionListener() {
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
    final JPopupMenu popup = new JPopupMenu("Categories");
    for(final TableCol col : tdata.cols) {
      final String item = (col.elem ? "" : "@") + string(col.name);
      final JMenuItem mi = new JCheckBoxMenuItem(item, col.width != 0);
      mi.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent ac) {
          final boolean sel = mi.isSelected();
          boolean vis = sel;
          // disallow removal of last visible column
          for(final TableCol c : tdata.cols) vis |= c != col && c.width != 0;

          if(vis) {
            final double vw = sel ? col.hwidth : 0;
            final double hw = sel ? 0 : col.width;
            col.hwidth = hw;
            col.width = vw;
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
    if(inputCol != col) {
      if(box != null) box.stop();
      box = new TableInput(this, tdata.cols[col].filter);
      inputCol = col;
    }
    requestFocusInWindow();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    super.keyTyped(e);
    if(tdata.roots.size == 0) return;

    if(box == null || inputCol == -1 || e.isAltDown()) return;
    if(box.add(e.getKeyChar())) {
      tdata.cols[inputCol].filter = box.text;
      tdata.find();
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(tdata.roots.size == 0) return;

    GUI.get().checkKeys(e);
    shift = e.isShiftDown();
    alt = e.isAltDown();
    if(box == null && alt || inputCol == -1) return;

    final int c = e.getKeyCode();
    if(c == KeyEvent.VK_ENTER) {
      box.stop();
      inputCol = -1;
      final Nodes marked = GUI.context.marked();
      if(marked.size != 0) View.notifyContext(marked, false, null);
    } else if(c == KeyEvent.VK_TAB) {
      if(box != null) {
        tdata.cols[inputCol].filter = box.text;
        box.stop();
      }
      inputCol = (inputCol + (shift ? -1 : 1)) % tdata.cols.length;
      if(inputCol == -1) inputCol = tdata.cols.length - 1;
      box = new TableInput(TableHeader.this, tdata.cols[inputCol].filter);
    } else {
      box.code(c);
    }
    repaint();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if(tdata.roots == null || tdata.roots.size == 0) return;
    shift = e.isShiftDown();
    alt = e.isAltDown();
  }
}

