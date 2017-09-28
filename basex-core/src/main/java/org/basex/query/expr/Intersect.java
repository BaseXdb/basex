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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Intersect extends Set {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public Intersect(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);
    return oneIsEmpty() ? cc.emptySeq(this) : this;
  }

  @Override
  protected ANodeBuilder eval(final Iter[] iters, final QueryContext qc) throws QueryException {
    ANodeBuilder list = new ANodeBuilder();
    for(Item it; (it = iters[0].next()) != null;) {
      qc.checkStop();
      list.add(toNode(it));
    }

    final int el = exprs.length;
    for(int e = 1; e < el && !list.isEmpty(); ++e) {
      list.check();
      final ANodeBuilder nt = new ANodeBuilder();
      final Iter iter = iters[e];
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        final ANode n = toNode(it);
        if(list.contains(n)) nt.add(n);
      }
      list = nt;
    }
    return list;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Intersect is = new Intersect(info, copyAll(cc, vm, exprs));
    is.iterable = iterable;
    return copyType(is);
  }

  @Override
  protected NodeIter iter(final Iter[] iters) {
    return new SetIter(iters) {
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

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Intersect && super.equals(obj);
  }
}
