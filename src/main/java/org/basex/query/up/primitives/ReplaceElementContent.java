package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace element content primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceElementContent extends StructuralUpdate {
  /** New value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param p pre
   * @param d data
   * @param i input info
   * @param val new value
   */
  public ReplaceElementContent(final int p, final Data d, final InputInfo i,
      final byte[] val) {
    super(PrimitiveType.REPLACEELEMCONT, p, d, i);
    value = val;
  }

  @Override
  public void apply() {
    final int loc = pre + data.attSize(pre, Data.ELEM);
    /* attributes of target node are not affected - d.attSize
     * returns number of attributes + 1, so d.attSize + value.length is
     * correct new size as only a text node is (evtl.) inserted
     */
    shifts = data.attSize(loc, data.kind(loc)) + value.length -
    data.size(loc, data.kind(loc));

    if(pre + data.size(pre, Data.ELEM) == loc + 1 &&
        data.kind(loc) == Data.TEXT) {
      // overwrite existing text node
      data.replace(loc, Data.TEXT, value);
    } else {
      while(pre + data.size(pre, Data.ELEM) > loc) data.delete(loc);
      if(value.length > 0) {
        final MemData md = new MemData(data);
        md.text(0, loc - pre, value, Data.TEXT);
        md.insert(0);
        data.insert(loc, pre, md);
      }
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPV.thrw(input, targetNode());
  }

  @Override
  public boolean checkTextAdjacency(final int c) {
    return false;
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.name(this), targetNode(), value);
  }
}
