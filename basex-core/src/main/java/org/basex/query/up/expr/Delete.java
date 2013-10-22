package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Delete expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param r return expression
   */
  public Delete(final StaticContext sctx, final InputInfo ii, final Expr r) {
    super(sctx, ii, r);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter t = ctx.iter(expr[0]);
    for(Item i; (i = t.next()) != null;) {
      if(!(i instanceof ANode)) UPTRGDELEMPT.thrw(info);
      final ANode n = (ANode) i;
      // nodes without parents are ignored
      if(n.parent() == null) continue;
      final DBNode dbn = ctx.updates.determineDataRef(n, ctx);
      ctx.updates.add(new DeleteNode(dbn.pre, dbn.data, info), ctx);
    }
    return null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Delete(sc, info, expr[0].copy(ctx, scp, vs));
  }

  @Override
  public String toString() {
    return DELETE + ' ' + NODES + ' ' + expr[0];
  }
}
