package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Except expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Except extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Except(final InputInfo ii, final Expr[] e) {
    super(ii, e);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    final int es = expr.length;
    final ExprList el = new ExprList(es);
    for(final Expr ex : expr) {
      if(ex.isEmpty()) {
        // remove empty operands (return empty sequence if first value is empty)
        if(el.isEmpty()) return optPre(null, ctx);
        ctx.compInfo(OPTREMOVE, this, ex);
      } else {
        el.add(ex);
      }
    }
    // ensure that results are always sorted
    if(el.size() == 1 && iterable) return el.get(0);
    // replace expressions with optimized list
    if(el.size() != es) expr = el.finish();
    return this;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Except ex = new Except(info, copyAll(ctx, scp, vs, expr));
    ex.iterable = iterable;
    return copyType(ex);
  }

  @Override
  protected NodeSeqBuilder eval(final Iter[] iter) throws QueryException {
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();

    for(Item it; (it = iter[0].next()) != null;) nc.add(checkNode(it));
    final boolean db = nc.dbnodes();

    for(int e = 1; e != expr.length && nc.size() != 0; ++e) {
      final Iter ir = iter[e];
      for(Item it; (it = ir.next()) != null;) {
        final int i = nc.indexOf(checkNode(it), db);
        if(i != -1) nc.delete(i);
      }
    }
    return nc;
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public ANode next() throws QueryException {
        if(item == null) {
          item = new ANode[iter.length];
          for(int i = 0; i != iter.length; ++i) next(i);
        }

        for(int i = 1; i != item.length; ++i) {
          if(item[0] == null) return null;
          if(item[i] == null) continue;
          final int d = item[0].diff(item[i]);

          if(d < 0 && i + 1 == item.length) break;
          if(d == 0) {
            next(0);
            i = 0;
          }
          if(d > 0) next(i--);
        }
        final ANode temp = item[0];
        next(0);
        return temp;
      }
    };
  }
}
