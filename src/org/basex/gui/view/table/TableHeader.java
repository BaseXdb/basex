package org.basex.gui.view.table;

import static org.basex.Text.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.gui.view.View;

/**
 * This is the header of the table view.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableHeader extends BaseXPanel {
  /** View reference. */
  protected TableView view;
  /** Table Data. */
  protected TableData tdata;
  /** Clicked column. */
  protected int clickCol = -1;
  /** Input column. */
  protected int inputCol = -1;
  /** Moved column. */
  protected int moveC = -1;
  /** Moved X position. */
  protected int mouseX = -1;
  /** Shift flag. */
  protected boolean shift;
  /** Ctrl flag. */
  protected boolean ctrl;
  /** Alt flag. */
  protected boolean alt;
  /** Input Box. */
  protected transient TableInput box;

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
    
    g.setFont(GUIConstants.font);
    if(tdata.cols == null || tdata.colW == null || tdata.cols.size == 0) {
      g.setColor(Color.black);
      g.drawString(NOTABLE, 8, GUIProp.fontsize + 4);
      return;
    }
    
    final int bs = BaseXBar.SIZE;
    int w = getWidth();
    final int h = getHeight();
    final int hh = h >> 1;
    g.setColor(Color.white);
    g.drawLine(0, h - 1, w, h - 1);

    w -= bs;
    final int nc = tdata.cols.size;
    double x = 0;
    for(int n = 0; n < nc; n++) {
      final double cw = w * tdata.colW[n];
      final double ce = x + cw;

      // header
      BaseXLayout.drawCell(g, (int) x, (int) ce, 0, hh, false);
      // input field
      g.setColor(Color.white);
      g.fillRect((int) x + 1, hh, (int) ce - (int) x - 2, hh - 2);
      g.drawLine((int) ce - 1, hh - 1, (int) ce - 1, h - 2);
      g.setColor(GUIConstants.color5);
      g.drawLine((int) ce, hh - 1, (int) ce, h - 2);
      
      // header
      g.setColor(GUIConstants.color6);
      g.setFont(GUIConstants.bfont);
      final int off = n == clickCol ? 1 : 0;
      BaseXLayout.chopString(g, tdata.colNames.list[n],
          (int) x + 4 + off, 2 + off, (int) cw);
      
      if(n == tdata.sortCol) {
        if(tdata.asc) g.drawPolygon(new int[] { (int) ce - 12 + off,
            (int) ce - 6 + off, (int) ce - 9 + off },
            new int[] { 4 + off, 4 + off, 8 + off }, 3);
        else g.drawPolygon(new int[] { (int) ce - 12 + off, (int) ce - 6 + off,
            (int) ce - 9 + off }, new int[] { 8 + off, 8 + off, 4 + off }, 3);
      }
      
      if(box != null && inputCol == n) {
        box.paint(g, (int) x, hh, (int) ce - (int) x, hh);
      } else {
        g.setColor(Color.black);
        g.setFont(GUIConstants.font);
        g.drawString(tdata.filter[n], (int) x + 5, h - 7);
      }
      x = ce;
    }
    final byte[] reset = { 'x' };
    int p = (bs - BaseXLayout.width(g, reset)) / 2;
    BaseXLayout.drawCell(g, (int) x, w + bs, 0, hh, false);
    BaseXLayout.drawCell(g, (int) x, w + bs, hh - 1, h, false);
    g.setColor(GUIConstants.color6);
    p += nc == clickCol ? 1 : 0;
    final int y = nc == clickCol ? 3 : 2;
    g.setFont(GUIConstants.bfont);
    BaseXLayout.chopString(g, reset, (int) x + p, y, w);
    BaseXLayout.chopString(g, reset, (int) x + p, hh + y - 1, w);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
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
    for(int i = 0; i < tdata.cols.size; i++) {
      if(i > 0 && Math.abs(mx - x) < 3) return i;
      x += w * tdata.colW[i];
    }
    return -1;
  }



  @Override
  public void mouseReleased(final MouseEvent e) {
    if(header(e.getY())) {
      clickCol = -1;
      repaint();
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(moveC == -1) return;
    
    final int x = e.getX();
    final double p = (double) (x - mouseX) / (getWidth() - BaseXBar.SIZE);
    final double[] ww = tdata.colW.clone();
    
    if(e.isShiftDown()) {
      ww[moveC - 1] += p;
      ww[moveC] -= p;
    } else {
      for(int i = 0; i < moveC; i++) ww[i] += p / moveC;
      for(int i = moveC; i < ww.length; i++) ww[i] -= p / (ww.length - moveC);
    }
    for(double w : ww) if(w < 0.0001) return;
    mouseX = x;
    tdata.colW = ww;
    
    view.repaint();
  }
  
  @Override
  public void mouseExited(final MouseEvent e) {
    GUI.get().cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);

    if(!SwingUtilities.isLeftMouseButton(e)) return;
    
    // header
    final int col = tdata.column(getWidth(), mouseX);
    if(header(e.getY())) {
      if(moveC == -1) {
        // sort data in current column
        clickCol = col;
        tdata.asc = tdata.sortCol == col ? !tdata.asc : true;
        tdata.sortCol = col;
        if(col == -1) tdata.calcWidths();
        tdata.createRows();
        view.repaint();
      }
    } else {
      // activate table filter
      if(col != -1) {
        filter(col);
      } else {
        // reset table filter
        tdata.resetFilter();
        tdata.find();
      }
    }
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
      box = new TableInput(this, tdata.filter[col]);
      inputCol = col;
    }
    requestFocusInWindow();
  }
  
  @Override
  public void keyTyped(final KeyEvent e) {
    if(box == null || inputCol == -1 || e.isAltDown()) return;
    if(box.add(e.getKeyChar())) {
      tdata.filter[inputCol] = box.text;
      tdata.find();
    }
  }
  
  @Override
  public void keyPressed(final KeyEvent e) {
    GUI.get().browse(e);
    shift = e.isShiftDown();
    ctrl = e.isControlDown();
    alt = e.isAltDown();
    if(box == null && alt) return;
    
    final int c = e.getKeyCode();
    if(c == KeyEvent.VK_ENTER) {
      box.stop();
      inputCol = -1;
      final Nodes marked = GUI.context.marked();
      if(marked.size != 0) View.notifyContext(marked, false);
    } else if(c == KeyEvent.VK_TAB) {
      if(box != null) {
        tdata.filter[inputCol] = box.text;
        box.stop();
      }
      inputCol = (inputCol + (shift ? -1 : 1)) % tdata.filter.length;
      if(inputCol == -1) inputCol = tdata.filter.length - 1;
      box = new TableInput(TableHeader.this, tdata.filter[inputCol]);
    } else {
      box.code(c);
    }
    repaint();
  }
  
  @Override
  public void keyReleased(final KeyEvent e) {
    shift = e.isShiftDown();
    ctrl = e.isControlDown();
    alt = e.isAltDown();
  }
}

