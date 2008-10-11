package org.basex.gui.view.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIFS;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This is the content area of the table view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableContent extends BaseXBack {
  /** Scrollbar reference. */
  private final BaseXBar scroll;
  /** View reference. */
  private final TableData tdata;
  /** Currently focused string. */
  String focusedString;

  /**
   * Default constructor.
   * @param d table data
   * @param scr scrollbar reference
   */
  TableContent(final TableData d, final BaseXBar scr) {
    scroll = scr;
    setLayout(new BorderLayout());
    setMode(FILL.DOWN);
    add(scroll, BorderLayout.EAST);
    tdata = d;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final Context context = GUI.context;
    final Data data = context.data();
    // skip if view is unavailable
    if(data == null || tdata.cols == null || tdata.cols.size == 0 ||
        tdata.colW == null) return;

    g.setFont(GUIConstants.font);
    BaseXLayout.antiAlias(g);
    View.painting = true;

    final int w = getWidth() - scroll.getWidth();
    final int h = getHeight();

    final int rfocus = tdata.getRoot(data, View.focused);
    final int focus = View.focused;
    int mpos = 0;

    final int nCols = tdata.cols.size;
    final int nRows = tdata.rows.size;
    final int rowH = tdata.rowH;

    final TableIterator ti = new TableIterator(data, tdata);
    final TokenBuilder[] tb = new TokenBuilder[nCols];
    for(int i = 0; i < nCols; i++) tb[i] = new TokenBuilder();

    focusedString = null;
    final Nodes marked = context.marked();
    int l = scroll.pos() / rowH - 1;
    int posY = -scroll.pos() + l * rowH;

    while(++l < nRows) {
      // skip when all visible rows have been painted or if data has changed
      if(posY > h || l >= tdata.rows.size) break;
      posY += rowH;

      final int pre = tdata.rows.get(l);
      while(mpos < marked.size && marked.nodes[mpos] < pre) mpos++;

      // draw line
      g.setColor(GUIConstants.color3);
      g.drawLine(0, posY + rowH - 1, w, posY + rowH - 1);
      g.setColor(Color.white);
      g.drawLine(0, posY + rowH, w, posY + rowH);

      // verify if current node is marked or focused
      final boolean rm = mpos < marked.size && marked.nodes[mpos] == pre;
      final boolean rf = pre == rfocus;
      final int col = rm ? rf ? 5 : 4 : 3;
      if(rm || rf) {
        g.setColor(GUIConstants.COLORS[col]);
        g.fillRect(0, posY - 1, w, rowH);
        g.setColor(GUIConstants.COLORS[col + 4]);
        g.drawLine(0, posY - 1, w, posY - 1);
      }
      g.setColor(Color.black);

      // skip drawing of text during animation
      if(rowH < GUIProp.fontsize) continue;

      // find all row contents
      ti.init(pre);
      int fcol = -1;
      while(ti.more()) {
        final int c = ti.col;
        if(ti.pre == focus || data.parent(ti.pre, data.kind(ti.pre)) == focus)
            fcol = c;

        // add content to column...
        if(tb[c].size > 100) {
          // don't add content if string is already too long
          tb[c].add("...");
          break;
        }
        
        if(tb[c].size != 0) tb[c].add("; ");
        byte[] txt = ti.elem ? data.text(ti.pre) : data.attValue(ti.pre);
        if(tdata.cols.get(c) == data.atts.id(DataText.MTIME)) {
          txt = Token.token(new SimpleDateFormat("dd.MM.yyyy").
              format(new Date(Token.toLong(txt) * 60000)));
        }
        tb[c].add(txt);
      }

      // draw row contents
      byte[] focusStr = null;
      int fx = -1;
      double x = 0;
      for(int c = 0; c < nCols; c++) {
        // draw single column
        double cw = w * tdata.colW[c];
        final double ce = x + cw;

        final byte[] str = tb[c].size != 0 ? tb[c].finish() : null;
        if(str != null) {
          if(data.fs != null && c == 0) {
            g.drawImage(GUIFS.images(str, false), (int) x + 1, posY + 3, this);
            x += 20;
            cw -= 20;
          }
          if(tdata.mouseX > x && tdata.mouseX < ce || fcol == c) {
            fx = (int) x;
            focusStr = str;
          }
          BaseXLayout.chopString(g, str, (int) x + 2, posY + 2, (int) cw - 4);
          tb[c].reset();
        }
        x = ce;
      }

      // highlight focused entry
      if(rf || fcol != -1) {
        if(focusStr != null) {
          final int sw = BaseXLayout.width(g, focusStr) + 8;
          if(fx > w - sw - 2) fx = w - sw - 2;
          g.setColor(GUIConstants.COLORS[col + 2]);
          g.fillRect(fx - 2, posY, sw, rowH - 1);
          g.setColor(Color.black);
          BaseXLayout.chopString(g, focusStr, fx + 2, posY + 2, sw);

          // cache focused string
          focusedString = Token.string(focusStr);
          final int i = focusedString.indexOf("; ");
          if(i != -1) focusedString = focusedString.substring(0, i);
        }
      }
    }
    View.painting = false;
  }
}

