package org.basex.gui.view.table;

import org.basex.data.*;

/**
 * This is an iterator for parsing the rows' contents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class TableIterator {
  /** Table data. */
  private final TableData tdata;
  /** Data reference. */
  private final Data data;
  /** Last pre value. */
  private int last;
  /** Last tag. */
  private int tag;
  /** Root tag. */
  private int rootTag;

  /** Current pre value. */
  int pre;
  /** Current column. */
  int col;
  /** Element flag. */
  boolean text;

  /**
   * Default constructor.
   * @param d data reference
   * @param td table data
   */
  TableIterator(final Data d, final TableData td) {
    data = d;
    tdata = td;
  }

  /**
   * Initializes the iterator.
   * @param p pre value to start from (must be an element node)
   */
  void init(final int p) {
    last = p + data.size(p, data.kind(p));
    rootTag = data.name(p);
    pre = p;
    tag = -1;
  }

  /**
   * Checks if more values are found.
   * @return result of check
   */
  boolean more() {
    while(++pre < last) {
      final int k = data.kind(pre);
      text = k == Data.TEXT;

      // content found...
      if(text || k == Data.ATTR) {
        final int id = text ? tag : data.name(pre);
        // find correct column...
        for(col = 0; col < tdata.cols.length; ++col) {
          if(tdata.cols[col].id == id && tdata.cols[col].elem == text) {
            return true;
          }
        }
      } else if(k == Data.ELEM) {
        // remember last tag
        tag = data.name(pre);
        if(tag == rootTag) return false;
      }
    }
    return false;
  }
}
