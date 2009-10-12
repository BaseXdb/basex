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
public class ReplaceElemContentPrimitive extends UpdatePrimitive {
  /** Replacing text node. */ 
  public byte[] r;

  /**
   * Constructor.
   * @param n target node
   * @param tn replacing content
   */
  public ReplaceElemContentPrimitive(final Nod n, final byte[] tn) {
    super(n);
    r = tn;
  }

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @SuppressWarnings("unused")
  @Override
  public void apply() throws QueryException {
    if(!(node instanceof DBNode)) return;
    final DBNode n = (DBNode) node;
    final int p = n.pre;
    final Data d = n.data;
    final int j = p + d.attSize(p, Data.ELEM);
    int i = j;
    final int l = p + d.size(p, Data.ELEM);
    while(i < l) d.delete(i++);
    d.insert(j, p, r, Data.TEXT);
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    if(mult) Err.or(UPTRGMULT, node);
    mult = true;
  }

  @Override
  public Type type() {
    return Type.REPLACEELEMCONT;
  }

}
