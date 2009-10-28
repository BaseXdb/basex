package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.DeletePrimitive;
import org.basex.query.util.Err;

/**
 * Delete expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param r return expression
   */
  public Delete(final Expr r) {
    super(r);
  }
  
  @Override
  public Seq atomic(final QueryContext ctx) throws QueryException {
    final Iter t = SeqIter.get(expr[0].iter(ctx));
    Item i = t.next();
    while(i != null) {
      if(!(i instanceof Nod)) Err.or(UPTRGDELEMPT, this);
      ctx.updates.add(new DeletePrimitive((Nod) i));
      i = t.next();
    }
    return Seq.EMPTY;
  }

  @Override
  public String toString() {
    return DELETE + ' ' + NODES + ' ' + expr[0];
  }
}
