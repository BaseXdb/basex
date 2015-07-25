package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Intersect expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class InterSect extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public InterSect(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    super.optimize(qc, scp);
    return oneIsEmpty() ? optPre(qc) : this;
  }

  @Override
  protected ANodeList eval(final Iter[] iter) throws QueryException {
    ANodeList list = new ANodeList();

    for(Item it; (it = iter[0].next()) != null;) list.add(toNode(it));
    final boolean db = list.dbnodes();

    final int el = exprs.length;
    for(int e = 1; e < el && list.size() != 0; ++e) {
      final ANodeList nt = new ANodeList().check();
      final Iter ir = iter[e];
      for(Item it; (it = ir.next()) != null;) {
        final ANode n = toNode(it);
        final int i = list.indexOf(n, db);
        if(i != -1) nt.add(n);
      }
      list = nt;
    }
    return list;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final InterSect is = new InterSect(info, copyAll(qc, scp, vs, exprs));
    is.iterable = iterable;
    return copyType(is);
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public ANode next() throws QueryException {
        final int irl = iter.length;
        if(item == null) item = new ANode[irl];

        for(int i = 0; i < irl; i++) if(!next(i)) return null;

        final int il = item.length;
        for(int i = 1; i < il;) {
          final int d = item[0].diff(item[i]);
          if(d > 0) {
            if(!next(i)) return null;
          } else if(d < 0) {
            if(!next(0)) return null;
            i = 1;
          } else {
            ++i;
          }
        }
        return item[0];
      }
    };
  }
}
