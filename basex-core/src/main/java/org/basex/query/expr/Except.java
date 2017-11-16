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
 * Except expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    super.optimize(cc);

    final ExprList el = new ExprList(exprs.length);
    for(final Expr ex : exprs) {
      if(ex == Empty.SEQ) {
        // remove empty operands (return empty sequence if first value is empty)
        if(el.isEmpty()) return cc.emptySeq(this);
        cc.info(OPTREMOVE_X_X, ex, description());
      } else {
        el.add(ex);
      }
    }
    // ensure that results are always sorted
    if(el.size() == 1 && iterable) return el.get(0);
    // replace expressions with optimized list
    exprs = el.finish();
    return this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Except ex = new Except(info, copyAll(cc, vm, exprs));
    ex.iterable = iterable;
    return copyType(ex);
  }

  @Override
  protected ANodeBuilder eval(final Iter[] iters, final QueryContext qc) throws QueryException {
    final ANodeBuilder list = new ANodeBuilder();
    for(Item it; (it = iters[0].next()) != null;) {
      qc.checkStop();
      list.add(toNode(it));
    }
    list.check();

    final int el = exprs.length;
    for(int e = 1; e < el && !list.isEmpty(); e++) {
      final Iter iter = iters[e];
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        list.delete(toNode(it));
      }
    }
    return list;
  }

  @Override
  protected NodeIter iter(final Iter[] iters) {
    return new SetIter(iters) {
      @Override
      public ANode next() throws QueryException {
        if(nodes == null) {
          final int il = iter.length;
          nodes = new ANode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        final int il = nodes.length;
        for(int i = 1; i < il; i++) {
          if(nodes[0] == null) return null;
          if(nodes[i] == null) continue;
          final int d = nodes[0].diff(nodes[i]);

          if(d < 0 && i + 1 == il) break;
          if(d == 0) {
            next(0);
            i = 0;
          }
          if(d > 0) next(i--);
        }
        final ANode temp = nodes[0];
        next(0);
        return temp;
      }
    };
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Except && super.equals(obj);
  }
}
