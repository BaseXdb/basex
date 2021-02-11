package org.basex.gui.view.table;

import org.basex.data.*;
import org.basex.gui.view.table.TableData.TableCol;

/**
 * This is an iterator for parsing the rows' contents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class TableIterator {
  /** Table data. */
  private final TableData tdata;
  /** Data reference. */
  private final Data data;
  /** Current pre value. */
  private int last;
  /** Current element. */
  private int elem;
  /** Root element. */
  private int rootElem;

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
    rootElem = data.nameId(p);
    pre = p;
    elem = -1;
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
        final int id = text ? elem : data.nameId(pre);
        // find correct column...
        final TableCol[] cols = tdata.cols;
        final int cl = cols.length;
        for(col = 0; col < cl; ++col) {
          if(cols[col].id == id && cols[col].elem == text) return true;
        }
      } else if(k == Data.ELEM) {
        // remember name of last element
        elem = data.nameId(pre);
        if(elem == rootElem) return false;
      }
    }
    return false;
  }
}
