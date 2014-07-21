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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Except extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Except(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    super.optimize(qc, scp);

    final ExprList el = new ExprList(exprs.length);
    for(final Expr ex : exprs) {
      if(ex.isEmpty()) {
        // remove empty operands (return empty sequence if first value is empty)
        if(el.isEmpty()) return optPre(null, qc);
        qc.compInfo(OPTREMOVE, this, ex);
      } else {
        el.add(ex);
      }
    }
    // ensure that results are always sorted
    if(el.size() == 1 && iterable) return el.get(0);
    // replace expressions with optimized list
    exprs = el.array();
    return this;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Except ex = new Except(info, copyAll(qc, scp, vs, exprs));
    ex.iterable = iterable;
    return copyType(ex);
  }

  @Override
  protected NodeSeqBuilder eval(final Iter[] iter) throws QueryException {
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();

    for(Item it; (it = iter[0].next()) != null;) nc.add(checkNode(it));
    final boolean db = nc.dbnodes();

    for(int e = 1; e != exprs.length && nc.size() != 0; ++e) {
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
