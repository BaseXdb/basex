package org.basex.query.pf;

import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This is a simple container for string values.
 * Important: void columns would considerably improve performance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class Col {
  /** Values. */
  V[] r = new V[8];
  /** Column name. */
  int nm;
  /** Column type. */
  int tp;
  /** Number of strings. */
  int sz;

  /**
   * Constructor.
   * @param n column name
   * @param t column type
   */
  Col(final int n, final int t) { nm = n; tp = t; }

  /**
   * Adds a value to the array.
   * @param v value to be added
   */
  void a(final V v) {
    if(sz == r.length) r = Array.extend(r);
    r[sz++] = v;
  }

  /**
   * Deletes a row from the array.
   * @param i row to be deleted
   */
  void d(final int i) {
    if(i < --sz) System.arraycopy(r, i + 1, r, i, sz - i);
  }

  /**
   * Returns the value of the specified row.
   * @param p row position
   * @return value
   */
  V r(final int p) { return r[p]; }

  /**
   * Returns the value array.
   * @return value array
   */
  V[] r() { return r; }

  /**
   * Sets the specified value at the specified position.
   * @param p position
   * @param v value to be added
   */
  void r(final V v, final int p) { r[p] = v; }

  /**
   * Assigns rows of the specified column.
   * @param c columns to be set
   */
  void r(final Col c) { r = c.r; sz = c.sz; }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    final int m = 24;
    final int s = Math.min(m, sz);
    for(int c = 0; c < s; c++) {
      if(c != 0) tb.add(" ");
      tb.add(r(c).toString());
    }
    if(sz > m) tb.add("... ");
    return tb.toString();
  }
}
