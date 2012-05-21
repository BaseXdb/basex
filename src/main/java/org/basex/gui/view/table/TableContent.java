package org.basex.gui.view.table;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * This is the content area of the table view.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class TableContent extends BaseXBack {
  /** Scrollbar reference. */
  private final BaseXBar scroll;
  /** View reference. */
  private final TableData tdata;
  /** GUI reference. */
  private final GUI gui;
  /** Currently focused string. */
  String focusedString;

  /**
   * Default constructor.
   * @param d table data
   * @param scr scrollbar reference
   */
  TableContent(final TableData d, final BaseXBar scr) {
    scroll = scr;
    tdata = d;
    gui = scr.gui;
    layout(new BorderLayout());
    mode(gui.gprop.is(GUIProp.GRADIENT) ? Fill.GRADIENT : Fill.NONE);
    add(scroll, BorderLayout.EAST);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    // skip if view is unavailable
    if(tdata.rows == null) return;

    gui.painting = true;
    g.setFont(GUIConstants.font);

    final int w = getWidth() - scroll.getWidth();
    final int h = getHeight();
    final int fsz = gui.gprop.num(GUIProp.FONTSIZE);

    final Context context = tdata.context;
    final Data data = context.data();
    final int focus = gui.context.focused;
    final int rfocus = tdata.getRoot(data, focus);
    int mpos = 0;

    final int nCols = tdata.cols.length;
    final int nRows = tdata.rows.size();
    final int rowH = tdata.rowH;

    final TableIterator ti = new TableIterator(data, tdata);
    final TokenBuilder[] tb = new TokenBuilder[nCols];
    for(int i = 0; i < nCols; ++i) tb[i] = new TokenBuilder();

    focusedString = null;
    final Nodes marked = context.marked;
    int l = scroll.pos() / rowH - 1;
    int posY = -scroll.pos() + l * rowH;

    while(++l < nRows && marked != null) {
      // skip when all visible rows have been painted or if data has changed
      if(posY > h || l >= tdata.rows.size()) break;
      posY += rowH;

      final int pre = tdata.rows.get(l);
      while(mpos < marked.size() && marked.pres[mpos] < pre) ++mpos;

      // draw line
      g.setColor(GUIConstants.color2);
      g.drawLine(0, posY + rowH - 1, w, posY + rowH - 1);
      g.setColor(Color.white);
      g.drawLine(0, posY + rowH, w, posY + rowH);

      // verify if current node is marked or focused
      final boolean rm = mpos < marked.size() && marked.pres[mpos] == pre;
      final boolean rf = pre == rfocus;
      final int col = rm ? rf ? 5 : 4 : 3;
      if(rm || rf) {
        g.setColor(GUIConstants.color(col));
        g.fillRect(0, posY - 1, w, rowH);
        g.setColor(GUIConstants.color(col + 4));
        g.drawLine(0, posY - 1, w, posY - 1);
      }
      g.setColor(Color.black);

      // skip drawing of text during animation
      if(rowH < fsz) continue;

      // find all row contents
      ti.init(pre);
      int fcol = -1;
      while(ti.more()) {
        final int c = ti.col;
        if(ti.pre == focus || data.parent(ti.pre, data.kind(ti.pre)) == focus)
            fcol = c;

        // add content to column (skip too long contents)...
        if(tb[c].size() < 100) {
          if(!tb[c].isEmpty()) tb[c].add("; ");
          tb[c].add(data.text(ti.pre, ti.text));
        }
      }

      // add dots if content is too long
      for(final TokenBuilder t : tb) if(t.size() > 100) t.add(DOTS);

      // draw row contents
      byte[] focusStr = null;
      int fx = -1;
      double x = 1;
      for(int c = 0; c < nCols; ++c) {
        // draw single column
        final double cw = w * tdata.cols[c].width;
        final double ce = x + cw;

        if(ce != 0) {
          final byte[] str = tb[c].isEmpty() ? null : tb[c].finish();
          if(str != null) {
            if(tdata.mouseX > x && tdata.mouseX < ce || fcol == c) {
              fx = (int) x;
              focusStr = str;
            }
            BaseXLayout.chopString(g, str, (int) x + 1, posY + 2,
                (int) cw - 4, fsz);
            tb[c].reset();
          }
        }
        x = ce;
      }

      // highlight focused entry
      if(rf || fcol != -1) {
        if(focusStr != null) {
          final int sw = BaseXLayout.width(g, focusStr) + 8;
          if(fx > w - sw - 2) fx = w - sw - 2;
          g.setColor(GUIConstants.color(col + 2));
          g.fillRect(fx - 2, posY, sw, rowH - 1);
          g.setColor(Color.black);
          BaseXLayout.chopString(g, focusStr, fx + 1, posY + 2, sw, fsz);

          // cache focused string
          focusedString = string(focusStr);
          final int i = focusedString.indexOf("; ");
          if(i != -1) focusedString = focusedString.substring(0, i);
        }
      }
    }
    gui.painting = false;
  }
}

