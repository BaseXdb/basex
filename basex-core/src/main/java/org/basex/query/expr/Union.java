package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Union expression.
 *
 * @author BaseX Team 2005-19, BSD License
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE) {
        // remove empty operands
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    // no expressions: return empty sequence
    if(list.isEmpty()) return Empty.VALUE;
    // ensure that results are always sorted
    if(list.size() == 1 && iterable) return list.get(0);
    // replace expressions with optimized list
    exprs = list.finish();
    return this;
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder nodes = new ANodeBuilder();
    for(final Expr expr : exprs) {
      final Iter iter = expr.iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) nodes.add(toNode(item));
    }
    return nodes.value();
  }

  @Override
  protected NodeIter iterate(final QueryContext qc) throws QueryException {
    return new SetIter(qc, iters(qc)) {
      @Override
      public ANode next() throws QueryException {
        if(nodes == null) {
          final int il = iter.length;
          nodes = new ANode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        int m = -1;
        final int il = nodes.length;
        for(int i = 0; i < il; i++) {
          if(nodes[i] == null) continue;
          final int d = m == -1 ? 1 : nodes[m].diff(nodes[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;

        final ANode node = nodes[m];
        next(m);
        return node;
      }
    };
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Union un = new Union(info, copyAll(cc, vm, exprs));
    un.iterable = iterable;
    return copyType(un);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Union && super.equals(obj);
  }
}
