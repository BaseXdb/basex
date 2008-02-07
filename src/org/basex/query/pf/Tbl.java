package org.basex.query.pf;

/**
 * This class represents a Pathfinder table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class Tbl {
  /** Table Values. */
  private Col[] col = new Col[0];
  /** Number of columns. */
  int size;

  /**
   * Adds a new column to the table.
   * @param n column name
   * @param t column type
   * @return added column
   */
  Col a(final int n, final int t) {
    return a(new Col(n, t));
  }

  /**
   * Adds the column references from the specified table.
   * Warning: only references are copied here.
   * @param t table to be attached
   */
  void a(final Tbl t) {
    for(int l = 0; l < t.size; l++) a(t.col[l]);
  }

  /**
   * Adds the specified column to the table.
   * @param c column to be added
   * @return added column
   */
  private Col a(final Col c) {
    final Col[] t = new Col[size + 1];
    System.arraycopy(col, 0, t, 0, size);
    col = t;
    col[size++] = c;
    return c;
  }

  /**
   * Adopts the table schema of the specified table.
   * @param tb table to be attached
   */
  void s(final Tbl tb) {
    for(int c = 0; c < tb.size; c++) s(tb.c(c));
  }

  /**
   * Adopts a schema of the specified column.
   * @param c column to be added
   * @return added column
   */
  Col s(final Col c) {
    return a(c.nm, c.tp);
  }

  /**
   * Returns the specified column.
   * @param p column position
   * @return column
   */
  Col c(final int p) {
    return col[p];
  }

  /**
   * Returns the column position for the specified name.
   * No warning is thrown for non-existent columns.
   * @param n name to be found
   * @return column
   */
  int p(final int n) {
    for(int l = 0; l < size; l++) if(col[l].nm == n) return l;
    return -1;
  }

  /**
   * Attaches columns of the specified table into the current table.
   * Columns which are not found in the current table are ignored.
   * Warning: only references are copied here.
   * @param t table to be attached
   */
  void addOLD(final Tbl t) {
    for(int l = 0; l < t.size; l++) {
      final int n = p(t.col[l].nm);
      if(n != -1) col[n] = t.col[l];
    }
  }
}
