package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Union expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Union extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Union(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    final int es = expr.length;
    final ExprList el = new ExprList(es);
    for(final Expr ex : expr) {
      if(ex.isEmpty()) {
        // remove empty operands
        ctx.compInfo(OPTREMOVE, this, ex);
      } else {
        el.add(ex);
      }
    }
    // no expressions: return empty sequence
    if(el.isEmpty()) return Empty.SEQ;
    // ensure that results are always sorted
    if(el.size() == 1 && iterable) return el.get(0);
    // replace expressions with optimized list
    if(el.size() != es) expr = el.finish();
    return this;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    final Union un = new Union(info, copyAll(ctx, scp, vs, expr));
    un.iterable = iterable;
    return copyType(un);
  }

  @Override
  protected NodeSeqBuilder eval(final Iter[] iter) throws QueryException {
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();
    for(final Iter ir : iter) {
      for(Item it; (it = ir.next()) != null;) nc.add(checkNode(it));
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

        int m = -1;
        for(int i = 0; i != item.length; ++i) {
          if(item[i] == null) continue;
          final int d = m == -1 ? 1 : item[m].diff(item[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;

        final ANode it = item[m];
        next(m);
        return it;
      }
    };
  }
}
