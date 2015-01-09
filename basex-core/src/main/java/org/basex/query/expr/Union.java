package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Union expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Union extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Union(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    super.optimize(qc, scp);

    final ExprList el = new ExprList(exprs.length);
    for(final Expr ex : exprs) {
      if(ex.isEmpty()) {
        // remove empty operands
        qc.compInfo(OPTREMOVE, this, ex);
      } else {
        el.add(ex);
      }
    }
    // no expressions: return empty sequence
    if(el.isEmpty()) return Empty.SEQ;
    // ensure that results are always sorted
    if(el.size() == 1 && iterable) return el.get(0);
    // replace expressions with optimized list
    exprs = el.finish();
    return this;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Union un = new Union(info, copyAll(qc, scp, vs, exprs));
    un.iterable = iterable;
    return copyType(un);
  }

  @Override
  protected NodeSeqBuilder eval(final Iter[] iter) throws QueryException {
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();
    for(final Iter ir : iter) {
      for(Item it; (it = ir.next()) != null;) nc.add(toNode(it));
    }
    return nc;
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public ANode next() throws QueryException {
        if(item == null) {
          final int il = iter.length;
          item = new ANode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        int m = -1;
        final int il = item.length;
        for(int i = 0; i < il; i++) {
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
