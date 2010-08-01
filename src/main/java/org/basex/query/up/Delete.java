package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.DeletePrimitive;

/**
 * Delete expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param i query info
   * @param r return expression
   */
  public Delete(final QueryInfo i, final Expr r) {
    super(i, r);
  }

  @Override
  public Seq atomic(final QueryContext ctx) throws QueryException {
    final Iter t = expr[0].iter(ctx);
    Item i;
    while((i = t.next()) != null) {
      if(!(i instanceof Nod)) error(UPTRGDELEMPT);
      final Nod n = (Nod) i;
      // nodes without parents are ignored
      if(n.parent() != null) ctx.updates.add(new DeletePrimitive(this, n), ctx);
    }
    return Seq.EMPTY;
  }

  @Override
  public String toString() {
    return DELETE + ' ' + NODES + ' ' + expr[0];
  }
}
