package org.basex.gui.view.table;

import static org.basex.util.Token.*;

import org.basex.core.Context;
import org.basex.core.cmd.Find;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIProp;
import org.basex.index.Kind;
import org.basex.index.Names;
import org.basex.util.Array;
import org.basex.util.list.BoolList;
import org.basex.util.list.IntList;
import org.basex.util.list.StringList;
import org.basex.util.list.TokenList;

/**
 * This is a container for the table data.
 *
 * @author BaseX Team 2005-11, BSD License
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
  final Context context;
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
  /** ID of the table root tag. */
  int root;

  /** Window properties. */
  private final GUIProp gprop;
  /** Last query. */
  private String last = "";

  /** Table Column. */
  static final class TableCol {
    /** Tag/Attribute flag. */
    byte[] name;
    /** Column IDs. */
    int id;
    /** Tag/Attribute flags. */
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
   * @param pr gui properties
   */
  TableData(final Context ctx, final GUIProp pr) {
    context = ctx;
    gprop = pr;
  }

  /**
   * Initializes the table data.
   * @param data data reference
   */
  void init(final Data data) {
    roots = new TokenList();
    // sort keys by occurrence
    for(final byte[] k : data.pthindex.desc(EMPTY, true, true)) {
      int c = 0;
      for(final byte[] kk : data.pthindex.desc(k, true, false)) {
        final Names nm = startsWith(kk, '@') ? data.atnindex : data.tagindex;
        if(nm.stat(nm.id(delete(kk, '@'))).leaf) ++c;
      }
      // add keys with a minimum of three columns
      if(c > 2) roots.add(k);
    }
    init(data, -1);
  }

  /**
   * Initializes the table data.
   * @param r optional root node (ignored if -1)
   * @param data data reference
   */
  void init(final Data data, final int r) {
    cols = new TableCol[0];
    root = r;
    sortCol = -1;
    last = "";
    rowH = 1;

    if(r == -1 && roots.size() == 0) return;
    if(root == -1) root = data.tagindex.id(roots.get(0));
    for(final byte[] k : data.pthindex.desc(
        data.tagindex.key(root), true, true)) {
      final boolean elem = !startsWith(k, '@');
      final byte[] key = delete(k, '@');
      final Names index = elem ? data.tagindex : data.atnindex;
      if(index.stat(index.id(key)).leaf) addCol(key, elem);
    }

    context(true);
  }

  /**
   * Initializes the table data.
   * @param create flag for creating new rows
   */
  void context(final boolean create) {
    if(cols.length == 0) return;

    final Nodes n = context.current();
    if(!create && n.root && rootRows != null) {
      rows = rootRows;
      sortCol = -1;
    } else {
      createRows();
      if(n.root) rootRows = rows;
    }
    if(cols[0].width == 0 && cols[0].hwidth == 0) calcWidths();
  }

  /**
   * Adds the specified tag or attribute as column if it exists in the data.
   * @param name tag/attribute to be added
   * @param elem element flag
   */
  private void addCol(final byte[] name, final boolean elem) {
    final Data data = context.data();
    final int id = (elem ? data.tagindex : data.atnindex).id(name);
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
    final Data data = context.data();
    final int[] n = context.current().list;

    rows = new IntList();
    for(int p : n) {
      final int s = p + data.size(p, data.kind(p));
      // find first root tag
      do {
        if(data.kind(p) == Data.ELEM && data.name(p) == root) break;
      } while(++p < s);

      // parse whole document and collect root tags
      while(p < s) {
        final int k = data.kind(p);
        if(k == Data.ELEM && data.name(p) == root) rows.add(p);
        p += data.attSize(p, k);
      }
    }
    sort();
  }

  /**
   * Calculates the column widths, based on the contents.
   */
  private void calcWidths() {
    if(cols.length == 0) return;

    final Data data = context.data();
    final int cs = cols.length;

    // scan first MAXROWS root tags
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
   * @param force force zero widths
   */
  void setWidths(final boolean force) {
    // calculate width of each column
    double sum = 0;
    final int cs = cols.length;
    for(int c = 0; c < cs; ++c) sum += cols[c].width;
    // avoid too small columns
    final double min = force ? 0.0 : 0.5;
    for(int c = 0; c < cs; ++c)
      cols[c].width = Math.max(min / cs, cols[c].width / sum);
    // recalculate column widths
    sum = 0;
    for(int c = 0; c < cs; ++c) sum += cols[c].width;
    // normalize widths
    for(int c = 0; c < cs; ++c) cols[c].width /= sum;
  }

  /**
   * Sorts column entries.
   */
  void sort() {
    if(sortCol == -1) return;
    final int c = cols[sortCol].id;
    final boolean e = cols[sortCol].elem;

    final Data data = context.data();
    final Names index = e ? data.tagindex : data.atnindex;
    final Kind kind = index.stat(c).kind;
    final boolean num = kind == Kind.INT || kind == Kind.DBL;

    final byte[][] tokens = new byte[rows.size()][];
    final int rs = rows.size();
    for(int r = 0; r < rs; ++r) {
      int p = rows.get(r);
      final int s = p + data.size(p, data.kind(p));
      while(p != s) {
        final int k = data.kind(p);
        if((e && k == Data.ELEM || !e && k == Data.ATTR) && data.name(p) == c) {
          tokens[r] = data.atom(p);
          break;
        }
        p += e ? data.attSize(p, k) : 1;
      }
      if(tokens[r] == null || tokens[r].length == 0) {
        tokens[r] = num ? MAXNUM : MAXTOK;
      }
    }
    rows.sort(tokens, num, asc);
  }

  /**
   * Returns possible root tag.
   * @param data data reference
   * @param pre pre value to start with
   * @return root
   */
  int getRoot(final Data data, final int pre) {
    if(pre == -1) return -1;
    int p = pre;
    int k = data.kind(p);
    while(p != -1 && (k != Data.ELEM || data.name(p) != root)) {
      p = data.parent(p, k);
      k = p != -1 ? data.kind(p) : 0;
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
    for(int i = 0; i < cols.length; ++i) {
      final double cw = w * cols[i].width;
      final double ce = cs + cw;
      if(mx > cs && mx < ce) return i;
      cs = ce;
    }
    return -1;
  }

  /**
   * Resets the filter entries.
   */
  void resetFilter() {
    for(int f = 0; f < cols.length; ++f) cols[f].filter = "";
  }

  /**
   * Builds and returns an XQuery for the filtered terms.
   * @return query
   */
  String find() {
    final Data data = context.data();
    final boolean r = rows == rootRows;
    final StringList filters = new StringList();
    final TokenList names = new TokenList();
    final BoolList elems = new BoolList();
    for(final TableCol col : cols) {
      filters.add(col.filter);
      names.add(col.name);
      elems.add(col.elem);
    }
    final String query = Find.findTable(filters, names, elems,
        data.tagindex.key(root), gprop.is(GUIProp.FILTERRT) || r);
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
    rowH = Math.max(1, (int) (f * gprop.num(GUIProp.FONTSIZE) * 7 / 4));
    return rowH;
  }
}
