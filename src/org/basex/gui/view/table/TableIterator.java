package org.basex.gui.view.table;

import org.basex.data.Data;

/**
 * This is an iterator for parsing the rows' contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TableIterator {
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
  boolean elem;

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
    rootTag = data.tagID(p);
    pre = p;
    tag = -1;
  }

  /**
   * Checks if more values are found.
   * @return result of check
   */
  boolean more() {
    while(true) {
      // skip attributes or descendant nodes
      pre += data.fs != null ? 1 : data.attSize(pre, data.kind(pre));
      if(pre >= last) return false;
      
      final int k = data.kind(pre);
      elem = k == Data.TEXT;

      // content found...
      if(elem || k == Data.ATTR) {
        final int t = elem ? tag : data.attNameID(pre);
        // find correct column...
        for(col = 0; col < tdata.cols.size; col++) {
          if(tdata.cols.list[col] == t && tdata.elms.list[col] == elem)
            return true;
        }
      } else if(k == Data.ELEM) {
        // remember last tag
        tag = data.tagID(pre);
        if(tag == rootTag) return false;
      }
    }
  }
}
