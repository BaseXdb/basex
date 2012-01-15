package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.DeleteNode;
import org.basex.util.InputInfo;

/**
 * Delete expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param ii input info
   * @param r return expression
   */
  public Delete(final InputInfo ii, final Expr r) {
    super(ii, r);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter t = ctx.iter(expr[0]);
    for(Item i; (i = t.next()) != null;) {
      if(!(i instanceof ANode)) UPTRGDELEMPT.thrw(input);
      final ANode n = (ANode) i;
      // nodes without parents are ignored
      if(n.parent() == null) continue;
      final DBNode dbn = ctx.updates.determineDataRef(n, ctx);
      ctx.updates.add(new DeleteNode(dbn.pre, dbn.data, input), ctx);
    }
    return null;
  }

  @Override
  public String toString() {
    return DELETE + ' ' + NODES + ' ' + expr[0];
  }
}
