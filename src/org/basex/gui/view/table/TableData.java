package org.basex.gui.view.table;

import static org.basex.util.Token.*;
import org.basex.core.Context;
import org.basex.core.proc.Find;
import org.basex.core.proc.XPath;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.index.Names;
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

  /** Root nodes. */
  TokenList roots;
  /** Row references. */
  IntList rows;
  /** Tag/Attribute flag. */
  TokenList colNames = new TokenList();
  /** Column IDs. */
  IntList cols = new IntList();
  /** Tag/Attribute flags. */
  BoolList elms = new BoolList();
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
    roots = new TokenList();
    for(final byte[] k : data.skel.desc(Token.EMPTY, true, true)) {
      if(data.skel.desc(k, true, false).size > 1) roots.add(k);
    }
    init(data, -1);
  }
  
  /**
   * Initializes the table data.
   * @param root optional root node (ignored if -1)
   * @param data data reference
   */
  void init(final Data data, final int root) {
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
      rootTag = root != -1 ? root : data.tags.id(DataText.FILE);
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
      createTable(root);
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
   * @param root optional root tag (ignored if -1)
   * ...better algorithm needed to support more documents!
   */
  void createTable(final int root) {
    if(roots.size == 0) return;
    
    final Data data = GUI.context.data();
    rootTag = root == -1 ? data.tags.id(roots.list[0]) : root;

    for(final byte[] k : data.skel.desc(data.tags.key(rootTag), true, true)) {
      final boolean elem = !startsWith(k, '@');
      final byte[] key = delete(k, '@');
      final Names index = elem ? data.tags : data.atts;
      final int id = index.id(key);
      if(index.noLeaf(id)) continue;
      cols.add(id);
      elms.add(elem);
      colNames.add(key);
    }
  }

  /**
   * Creates the row list for the specified nodes.
   */
  void createRows() {
    final Context context = GUI.context;
    final Data data = context.data();
    final int[] n = context.current().nodes;

    rows = new IntList();
    for(int c = 0; c < n.length; c++) {
      int p = n[c];

      final int s = p + data.size(p, data.kind(p));
      // find first root tag
      do {
        if(data.kind(p) == Data.ELEM && data.tagID(p) == rootTag) break;
      } while(++p < s);

      // parse whole document and collect root tags
      while(p < s) {
        final int k = data.kind(p);
        if(k == Data.ELEM && data.tagID(p) == rootTag) rows.add(p);
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
    final int cs = cols.size;
    colW = new double[cs];

    // scan first MAXROWS root tags
    final int nRows = rows.size;
    final TableIterator ti = new TableIterator(data, this);
    
    for(int l = 0; l < MAXROWS && l < nRows; l++) {
      final int pre = rows.list[l];

      // find all row contents
      ti.init(pre);
      while(ti.more()) {
        // add string length...
        //if(colW[c].size < 100) {
        colW[ti.col] += ti.elem ? data.textLen(ti.pre) :
          data.attValue(ti.pre).length;
      }
    }
    for(int c = 0; c < cs; c++) {
      System.out.println(c + ": " + colW[c]);
    }
    System.out.println("---------");

    // calculate width of each column
    double sum = 0;
    for(int c = 0; c < cs; c++) sum += colW[c];
    // avoid too small columns
    for(int c = 0; c < cs; c++) colW[c] = Math.max(0.5 / cs, colW[c] / sum);
    // normalize widths
    sum = 0;
    for(int c = 0; c < cs; c++) sum += colW[c];
    for(int c = 0; c < cs; c++) colW[c] /= sum;
    
    // sort columns by string lengths
    final TokenList tl = new TokenList();
    for(int c = 0; c < cs; c++) tl.add(Token.token(colW[c]));
    final IntList il = IntList.createOrder(tl.finish(), true, false);
    
    final double[] cw = new double[cs];
    final IntList co = new IntList();
    final BoolList ce = new BoolList();
    final TokenList cn = new TokenList();
    for(int c = 0; c < cs; c++) {
      final int i = il.list[c];
      cw[c] = colW[i];
      co.add(cols.list[i]);
      ce.add(elms.list[i]);
      cn.add(colNames.list[i]);
    }
    colW = cw;
    cols = co;
    elms = ce;
    colNames = cn;
  }

  /**
   * Sort columns.
   */
  private void sort() {
    if(sortCol == -1) return;
    final int c = cols.list[sortCol];
    final boolean e = elms.list[sortCol];

    final Data data = GUI.context.data();
    final boolean fs = data.fs != null;
    final byte[][] tokens = new byte[rows.size][];

    for(int r = 0; r < rows.size; r++) {
      int p = rows.list[r];
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
    double cs = 0;
    for(int i = 0; i < cols.size; i++) {
      final double cw = w * colW[i];
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
