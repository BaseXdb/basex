package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;

import org.basex.data.MemData;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.iter.Iter;
import org.basex.query.up.PendingUpdates;
import org.basex.query.up.UpdateFunctions;

/**
 * Transform expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final For[] copies;

  /**
   * Constructor.
   * @param c copy expressions
   * @param m modify expression
   * @param r return expression
   */
  public Transform(final For[] c, final Expr m, final Expr r) {
    super(m, r);
    copies = c;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final int c = ctx.vars.size();
    for(final For fo : copies) {
      // [LK] copied node is a DOC node ? ... attributes ?
      final MemData m = UpdateFunctions.buildDB(fo.expr.iter(ctx), null);
      fo.var.bind(new DBNode(m, 1), ctx);
    }
    
    final PendingUpdates upd = ctx.updates;
    ctx.updates = new PendingUpdates();
    expr[0].iter(ctx);
    ctx.updates.applyUpdates();
    ctx.updates = upd;

    ctx.vars.reset(c);
    return expr[1].iter(ctx);
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(COPY + ' ');
    for(final ForLet t : copies) sb.append(t.var + ASSIGN + t.expr + ' ');
    return sb.append(MODIFY + ' ' + expr[0] + ' '  + RETURN + ' ' +
        expr[1]).toString();
  }
}
