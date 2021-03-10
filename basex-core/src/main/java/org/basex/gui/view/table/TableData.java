package org.basex.gui.view.table;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.index.name.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a container for the table data.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class TableData {
  /** Number of rows which is parsed for a new document. */
  private static final int MAXROWS = 1000;
  /** Maximum token for sorting data. */
  private static final byte[] MAXTOK = { -1 };
  /** Maximum number for sorting data. */
  private static final byte[] MAXNUM = token(Double.MAX_VALUE);

  /** Database context. */
  final Context ctx;
  /** Root nodes. */
  TokenList roots;
  /** Rows of the main table. */
  IntList rootRows;
  /** Pre values of the rows. */
  IntList rows;
  /** Columns. */
  TableCol[] cols;
  /** Row height. */
  int rowH;

  /** Currently sorted column. */
  int sortCol = -1;
  /** Sort order (ascending/descending). */
  boolean asc;

  /** Mouse X position. */
  int mouseX;
  /** Mouse Y position. */
  int mouseY;
  /** ID of the table root element. */
  int root;

  /** GUI options. */
  private final GUIOptions gopts;
  /** Last query. */
  private String last = "";

  /** Table Column. */
  static final class TableCol {
    /** Element/attribute name. */
    byte[] name;
    /** Column IDs. */
    int id;
    /** Element/attribute flag. */
    boolean elem;
    /** Column width. */
    double width;
    /** Hidden Column width. */
    double hwidth;
    /** Column filter. */
    String filter = "";
  }

  /**
   * Initializes the table data.
   * @param ctx database context
   * @param gopts gui options
   */
  TableData(final Context ctx, final GUIOptions gopts) {
    this.ctx = ctx;
    this.gopts = gopts;
  }

  /**
   * Initializes the table data.
   * @param data data reference
   */
  void init(final Data data) {
    roots = new TokenList();
    // sort keys by occurrence
    for(final byte[] k : data.paths.desc(EMPTY, true, true)) {
      int c = 0;
      for(final byte[] kk : data.paths.desc(k, true, false)) {
        final Names names = startsWith(kk, '@') ? data.attrNames : data.elemNames;
        if(names.stats(names.id(delete(kk, '@'))).isLeaf()) ++c;
      }
      // add keys with a minimum of three columns
      if(c > 2) roots.add(k);
    }
    init(data, -1);
  }

  /**
   * Initializes the table data.
   * @param rt optional root node (ignored if -1)
   * @param dt data reference
   */
  void init(final Data dt, final int rt) {
    cols = new TableCol[0];
    root = rt;
    sortCol = -1;
    last = "";
    rowH = 1;

    if(rt == -1 && roots.isEmpty()) return;
    if(root == -1) root = dt.elemNames.id(roots.get(0));
    for(final byte[] k : dt.paths.desc(dt.elemNames.key(root), true, true)) {
      final boolean elem = !startsWith(k, '@');
      final byte[] key = delete(k, '@');
      final Names names = elem ? dt.elemNames : dt.attrNames;
      if(names.stats(names.id(key)).isLeaf()) addCol(key, elem);
    }

    context(true);
  }

  /**
   * Initializes the table data.
   * @param create flag for creating new rows
   */
  void context(final boolean create) {
    if(cols.length == 0) return;

    final boolean rt = ctx.root();
    if(!create && rt && rootRows != null) {
      rows = rootRows;
      sortCol = -1;
    } else {
      createRows();
      if(rt) rootRows = rows;
    }
    if(cols[0].width == 0 && cols[0].hwidth == 0) calcWidths();
  }

  /**
   * Adds the specified element or attribute as column if it exists in the data.
   * @param name element/attribute name to be added
   * @param elem element flag
   */
  private void addCol(final byte[] name, final boolean elem) {
    final Data data = ctx.data();
    final int id = (elem ? data.elemNames : data.attrNames).id(name);
    if(id == 0) return;
    final TableCol col = new TableCol();
    col.id = id;
    col.elem = elem;
    col.name = name;
    cols = Array.add(cols, col);
  }

  /**
   * Creates the row list for the specified nodes.
   */
  private void createRows() {
    final Data data = ctx.data();
    rows = new IntList();
    for(int pre : ctx.current().pres()) {
      if(pre >= data.meta.size) break;
      final int s = pre + data.size(pre, data.kind(pre));
      // find first root element name
      do {
        if(data.kind(pre) == Data.ELEM && data.nameId(pre) == root) break;
      } while(++pre < s);

      // parse whole document and collect root element names
      while(pre < s) {
        final int k = data.kind(pre);
        if(k == Data.ELEM && data.nameId(pre) == root) rows.add(pre);
        pre += data.attSize(pre, k);
      }
    }
    sort();
  }

  /**
   * Calculates the column widths, based on the contents.
   */
  private void calcWidths() {
    if(cols.length == 0) return;

    final Data data = ctx.data();
    final int cs = cols.length;

    // scan first MAXROWS root elements
    final int nRows = rows.size();
    final TableIterator ti = new TableIterator(data, this);

    final int ll = Math.min(nRows, MAXROWS);
    for(int l = 0; l < ll; ++l) {
      // find all row contents and add string lengths
      ti.init(rows.get(l));
      while(ti.more()) cols[ti.col].width += data.textLen(ti.pre, ti.text);
    }

    // sort columns by string lengths
    final double[] widths = new double[cs];
    for(int c = 0; c < cs; ++c) widths[c] = cols[c].width;
    final int[] il = Array.createOrder(widths, false);

    final TableCol[] cl = new TableCol[cs];
    for(int c = 0; c < cs; ++c) cl[c] = cols[il[c]];
    cols = cl;

    setWidths(false);
  }

  /**
   * Sets the column widths, based on the contents.
   * @param enforce enforce zero widths
   */
  void setWidths(final boolean enforce) {
    // calculate width of each column
    double sum = 0;
    for(final TableCol col : cols) sum += col.width;
    // avoid too small columns
    final double min = enforce ? 0.0 : 0.5;
    final int cs = cols.length;
    for(final TableCol col : cols) col.width = Math.max(min / cs, col.width / sum);
    // recalculate column widths
    sum = 0;
    for(final TableCol col : cols) sum += col.width;
    // normalize widths
    for(final TableCol col : cols) col.width /= sum;
  }

  /**
   * Sorts column entries.
   */
  void sort() {
    if(sortCol == -1) return;
    final int c = cols[sortCol].id;
    final boolean e = cols[sortCol].elem;

    final Data data = ctx.data();
    final Names index = e ? data.elemNames : data.attrNames;
    final boolean num = StatsType.isNumeric(index.stats(c).type);

    final byte[][] tokens = new byte[rows.size()][];
    final int rs = rows.size();
    for(int r = 0; r < rs; ++r) {
      int p = rows.get(r);
      final int s = p + data.size(p, data.kind(p));
      while(p != s) {
        final int k = data.kind(p);
        if((e ? k == Data.ELEM : k == Data.ATTR) && data.nameId(p) == c) {
          tokens[r] = data.atom(p);
          break;
        }
        p += e ? data.attSize(p, k) : 1;
      }
      if(tokens[r] == null || tokens[r].length == 0) {
        tokens[r] = num ? MAXNUM : MAXTOK;
      }
    }
    rows.sort(tokens, asc, num);
  }

  /**
   * Returns pre value of possible root element.
   * @param data data reference
   * @param pre pre value to start with
   * @return pre value of root element
   */
  int getRoot(final Data data, final int pre) {
    if(pre == -1) return -1;
    int p = pre;
    int k = data.kind(p);
    while(p != -1 && (k != Data.ELEM || data.nameId(p) != root)) {
      p = data.parent(p, k);
      k = p == -1 ? 0 : data.kind(p);
    }
    return p;
  }

  /**
   * Returns the column at the specified horizontal position.
   * @param w panel width
   * @param mx mouse position
   * @return column
   */
  int column(final int w, final int mx) {
    double cs = 0;
    final int cl = cols.length;
    for(int c = 0; c < cl; c++) {
      final double cw = w * cols[c].width, ce = cs + cw;
      if(mx > cs && mx < ce) return c;
      cs = ce;
    }
    return -1;
  }

  /**
   * Resets the filter entries.
   */
  void resetFilter() {
    for(final TableCol col : cols) col.filter = "";
  }

  /**
   * Builds and returns an XQuery for the filtered terms.
   * @return query, or {@code null} if the query is identical to the last one
   */
  String find() {
    final Data data = ctx.data();
    final boolean r = rows == rootRows;
    final StringList filters = new StringList();
    final TokenList names = new TokenList();
    final BoolList elems = new BoolList();
    for(final TableCol col : cols) {
      filters.add(col.filter);
      names.add(col.name);
      elems.add(col.elem);
    }
    final String query = Find.findTable(filters, names, elems, data.elemNames.key(root),
        gopts.get(GUIOptions.FILTERRT) || r);
    if(query.equals(last)) return null;
    last = query;
    return query;
  }

  /**
   * Sets and returns the row height for the given factor.
   * @param f factor (1 = maximum/default)
   * @return row height
   */
  int rowH(final double f) {
    rowH = Math.max(1, (int) (f * GUIConstants.fontSize * 7 / 4));
    return rowH;
  }
}
