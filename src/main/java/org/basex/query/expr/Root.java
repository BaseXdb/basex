package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Root node.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Root extends Simple {
  /**
   * Constructor.
   * @param ii input info
   */
  public Root(final InputInfo ii) {
    super(ii);
    type = SeqType.NOD_ZM;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = checkCtx(ctx).iter(ctx);
    final NodIter ni = new NodIter().random();
    Item i;
    while((i = iter.next()) != null) {
      final Nod n = root(i);
      if(n == null || n.type != Type.DOC) Err.or(input, CTXNODE);
      ni.add(n);
    }
    return ni;
  }

  /**
   * Returns the root node of the specified item.
   * @param v input node
   * @return root node
   */
  public Nod root(final Value v) {
    if(!v.node()) return null;
    Nod n = (Nod) v;
    while(true) {
      final Nod p = n.parent();
      if(p == null) return n;
      n = p;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }

  @Override
  public boolean duplicates() {
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Root;
  }

  @Override
  public String toString() {
    return "root()";
  }
}
