package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Replace element content primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceElemContent extends UpdatePrimitive {
  /** Replacing text node. */
  private final byte[] txt;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param tn replacing content
   */
  public ReplaceElemContent(final InputInfo ii, final ANode n,
      final byte[] tn) {
    super(ii, n);
    txt = tn;
  }

  @Override
  public void apply(final int add) {
    final DBNode n = (DBNode) node;
    final int par = n.pre + add;
    final Data d = n.data;
    final int pre = par + d.attSize(par, Data.ELEM);
    while(par + d.size(par, Data.ELEM) > pre) {
      d.delete(pre);
    }
    if(txt.length > 0) {
      final MemData md = new MemData(n.data);
      md.text(0, pre - par, txt, Data.TEXT);
      md.insert(0);
      d.insert(pre, par, md);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPV.thrw(input, node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACEELEMCONT;
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + node + ", " + Token.string(txt) + "]";
  }
}