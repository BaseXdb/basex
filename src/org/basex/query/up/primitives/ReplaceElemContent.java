package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;

import org.basex.data.Data;
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
  public byte[] r;

  /**
   * Constructor.
   * @param n target node
   * @param tn replacing content
   */
  public ReplaceElemContent(final Nod n, final byte[] tn) {
    super(n);
    r = tn;
  }

  @Override
  public void check() {
  }

  @Override
  public void apply(final int add) {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final int p = n.pre + add;
    final Data d = n.data;
//    InfoTable.pT(d, 0, -1);
    final int j = p + d.attSize(p, Data.ELEM);
    int i = p + d.size(p, Data.ELEM) - 1;
    while(i >= j) d.delete(i--);
    if(r.length > 0) d.insert(j, p, r, Data.TEXT);
//    InfoTable.pT(d, 0, -1);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    Err.or(UPMULTREPV, node);
  }

  @Override
  public Type type() {
    return Type.REPLACEELEMCONT;
  }
}