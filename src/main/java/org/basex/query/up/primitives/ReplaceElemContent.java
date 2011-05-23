package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace element content primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceElemContent extends Primitive {
  /** New value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param val new value
   */
  public ReplaceElemContent(final InputInfo ii, final ANode n,
      final byte[] val) {
    super(PrimitiveType.REPLACEELEMCONT, ii, n);
    value = val;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;
    final int par = n.pre + add;
    final int pre = par + d.attSize(par, Data.ELEM);

    if(par + d.size(par, Data.ELEM) == pre + 1 && d.kind(pre) == Data.TEXT) {
      // overwrite existing text node
      d.replace(pre, Data.TEXT, value);
    } else {
      while(par + d.size(par, Data.ELEM) > pre) d.delete(pre);
      if(value.length > 0) {
        final MemData md = new MemData(n.data);
        md.text(0, pre - par, value, Data.TEXT);
        md.insert(0);
        d.insert(pre, par, md);
      }
    }
  }

  @Override
  public void merge(final Primitive p) throws QueryException {
    UPMULTREPV.thrw(input, node);
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.name(this), node, value);
  }
}