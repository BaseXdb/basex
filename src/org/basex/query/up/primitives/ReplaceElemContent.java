package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;
import org.basex.query.util.Err;

/**
 * Replace element content primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class ReplaceElemContent extends UpdatePrimitive {
  /** Replacing text node. */
  private final byte[] txt;

  /**
   * Constructor.
   * @param n target node
   * @param tn replacing content
   */
  public ReplaceElemContent(final Nod n, final byte[] tn) {
    super(n);
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
      md.insert(0, md.text(0, pre - par, txt, Data.TEXT));
      d.insert(pre, par, md);
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPV, node);
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.REPLACEELEMCONT;
  }
}