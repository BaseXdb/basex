package org.basex.gui.view.table;

import org.basex.core.Context;
import org.basex.core.proc.Find;
import org.basex.core.proc.XPath;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * This is a container for the table data.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableData {
  /** Number of rows which is parsed for a new document. */
  private static final int MAXROWS = 1000;

  /** Row references. */
  IntList rows;
  /** Tag/Attribute flag. */
  TokenList colNames = new TokenList();
  /** Column IDs. */
  final IntList cols = new IntList();
  /** Tag/Attribute flags. */
  final BoolList elms = new BoolList();
  /** Column widths. */
  double[] colW;
  /** Row height. */
  int rowH;
  /** Invalid flag. */
  boolean invalid;

  /** Filter inputs. */
  String[] filter;
  /** Currently sorted column. */
  int sortCol = -1;
  /** Sort order (ascending/descending). */
  boolean asc;

  /** Mouse X position. */
  int mouseX;
  /** Mouse Y position. */
  int mouseY;

  /** References to the main table. */
  private IntList rootRows;
  /** ID of the table root tag. */
  private int rootTag;
  /** Last query. */
  private String last = "";

  /**
   * Initializes the table data.
   * @param data data reference
   */
  void init(final Data data) {
    colNames.reset();
    cols.reset();
    elms.reset();
    rootRows = null;
    rows = null;
    filter = null;
    rootTag = 0;
    sortCol = -1;
    colW = null;
    last = "";
    rowH = 1;

    if(data.fs != null) {
      rootTag = data.tags.id(DataText.FILE);
      addCol("suffix", false);
      addCol("name", false);
      addCol("size", false);
      addCol("mtime", false);
      addCol("Title", true);
      addCol("Width", true);
      addCol("Height", true);
      addCol("Album", true);
      addCol("Person", true);
      addCol("Bitrate", true);
      addCol("Seconds", true);
    } else {
      // choose root element and columns to be shown
      createTable();
    }
    filter = new String[cols.size];
    resetFilter();
    invalid = cols.size == 0;
  }

  /**
   * Initializes the table data.
   */
  void context() {
    final Nodes n = GUI.context.current();
    final boolean root = n.size == 1 && n.nodes[0] < 2;
    if(rootRows != null && root) {
      rows = rootRows;
      sortCol = -1;
    } else {
      createRows();
      if(root) rootRows = rows;
    }

    // calculate column widths
    if(colW == null) calcWidths();
  }

  /**
   * Adds the specified tag or attribute as column if it exists in the data.
   * @param name tag/attribute to be added
   * @param elem element flag
   */
  private void addCol(final String name, final boolean elem) {
    final Data data = GUI.context.data();
    final byte[] n = Token.token(name);
    final int id = elem ? data.tagID(n) : data.attNameID(n);
    if(id != 0) {
      cols.add(id);
      elms.add(elem);
      colNames.add(n);
    }
  }

  /**
   * Creates the table layout.
   * ...better algorithm needed to support more documents!
   */
  private void createTable() {
    final Data data = GUI.context.data();
    int p = 0;
    int pre = 0;
    while(++p < data.size) {
      final int kind = data.kind(p);
      // skip nodes other than elements
      if(kind != Data.ELEM) continue;

      final int tag = data.tagID(p);
      // get number of tag occurrences
      final int occ = data.tags.counter(rootTag);
      // select tag as root node if it occurs often, but not too often..
      if(occ > data.tags.counter(tag) / 2 && occ > data.size / 100) {
        createCols(pre);
        return;
      }
      rootTag = tag;
      pre = p;
    }
  }

  /**
   * Creates the columns of the table.
   * @param pre pre value to start from (root tag)
   */
  private void createCols(final int pre) {
    final Data data = GUI.context.data();
    final byte[] tag = data.tag(pre);

    final int s = pre + data.size(pre, data.kind(pre));
    if(data.size != s && !Token.eq(tag, data.tag(s))) return;

    // scan first MAXROWS root tags
    int p = pre;
    for(int i = 0; i < MAXROWS && p < data.size; i++) {
      final int size = p + data.size(p, data.kind(p));
      p += data.attSize(p, Data.ELEM);
      while(p < size) {
        final int k = data.kind(p);
        final int np = p + data.attSize(p, k);
        if(k == Data.ELEM && np != size && data.kind(np) == Data.TEXT) {
          final int nt = data.tagID(p);
          if(!cols.contains(nt)) {
            cols.add(nt);
            elms.add(true);
            colNames.add(data.tag(p));
          }
        }
        p += data.size(p, k);
      }
    }
  }

  /**
   * Creates the row list for the specified nodes.
   * ...recursive tags work for the file system, but not for other data.
   */
  void createRows() {
    final Context context = GUI.context;
    final Nodes n = context.current();
    final Data data = context.data();

    rows = new IntList();
    for(int c = 0; c < n.size; c++) {
      int p = n.nodes[c];

      final int s = p + data.size(p, data.kind(p));
      // find first root tag
      do {
        if(data.kind(p) == Data.ELEM && data.tagID(p) == rootTag) break;
      } while(++p < s);

      // parse whole document and collect root tags
      while(p < s) {
        final int k = data.kind(p);
        if(k == Data.ELEM && data.tagID(p) == rootTag) rows.add(p);
        //p += fs ? data.attSize(p, k) : data.size(p, k);
        p += data.attSize(p, k);
      }
    }
    sort();
  }

  /**
   * Calculates the column widths, based on the contents.
   */
  void calcWidths() {
    final Data data = GUI.context.data();
    colW = new double[cols.size];

    // scan first MAXROWS root tags
    final int nRows = rows.size;
    final TableIterator ti = new TableIterator(data, this);
    for(int l = 0; l < MAXROWS && l < nRows; l++) {
      final int pre = rows.get(l);

      // find all row contents
      ti.init(pre);
      while(ti.more()) {
        // add string length...
        colW[ti.col] += ti.elem ? data.textLen(ti.pre) :
          data.attValue(ti.pre).length;
      }
    }

    // calculate width of each column
    double sum = 0;
    for(int c = 0; c < cols.size; c++) sum += colW[c];
    // avoid too small columns
    for(int c = 0; c < cols.size; c++) {
      colW[c] = Math.max(1.0 / cols.size, colW[c] / sum);
    }
    // normalize widths
    sum = 0;
    for(int c = 0; c < cols.size; c++) sum += colW[c];
    for(int c = 0; c < cols.size; c++) colW[c] /= sum;
  }

  /**
   * Sort columns.
   */
  private void sort() {
    if(sortCol == -1) return;
    final int c = cols.get(sortCol);
    final boolean e = elms.list[sortCol];

    final Data data = GUI.context.data();
    final boolean fs = data.fs != null;
    final byte[][] tokens = new byte[rows.size][];

    for(int r = 0; r < rows.size; r++) {
      int p = rows.get(r);
      final int s = p + data.size(p, data.kind(p));
      while(p != s) {
        final int k = data.kind(p);
        if(e && k == Data.ELEM && data.tagID(p) == c ||
           !e && k == Data.ATTR && data.attNameID(p) == c) {
          tokens[r] = data.atom(p);
          break;
        }
        p += fs ? 1 : data.attSize(p, k);
      }
      if(tokens[r] == null || tokens[r].length == 0) tokens[r] = LAST;
      else if(tokens[r][0] < 0) tokens[r][0] = 126;
    }
    rows.sort(tokens, !e && fs &&
        (c == data.atts.id(DataText.SIZE) ||
         c == data.atts.id(DataText.MTIME)), asc);
  }

  /** Last token. */
  private static final byte[] LAST = { 127 };

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
    while(p != -1 && (k != Data.ELEM || data.tagID(p) != rootTag)) {
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
    double x = 0;
    for(int i = 0; i < cols.size; i++) {
      final double cw = w * colW[i];
      final double ce = x + cw;
      if(mx > x && mx < ce) return i;
      x = ce;
    }
    return -1;
  }

  /**
   * Resets the filter entries.
   */
  void resetFilter() {
    for(int f = 0; f < filter.length; f++) filter[f] = "";
  }

  /**
   * Builds an XPath query and executes a search for the filtered terms.
   */
  void find() {
    final Data data = GUI.context.data();
    final boolean root = rows == rootRows;
    final String xpath = Find.findTable(filter, colNames,
        data.tags.key(rootTag), data, GUIProp.filterrt || root);
    if(xpath.equals(last)) return;
    last = xpath;
    GUI.get().execute(new XPath(xpath.length() != 0 ? xpath : "/"));
  }

  /**
   * Sets and returns the row height for the given factor.
   * @param f factor (1 = maximum/default)
   * @return row height
   */
  int rowH(final double f) {
    rowH = Math.max(1, (int) (f * GUIProp.fontsize * 7 / 4));
    return rowH;
  }
}
