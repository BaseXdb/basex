package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Replace element content primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  public ReplaceElemContent(final InputInfo ii, final Nod n, final byte[] tn) {
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
    Err.or(input, UPMULTREPV, node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACEELEMCONT;
  }
}