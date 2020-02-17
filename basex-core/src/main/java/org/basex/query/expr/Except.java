package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Except expression.
 *
 * @author BaseX Team 2005-20, BSD License
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

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE) {
        // remove empty operands (return empty sequence if first value is empty)
        if(list.isEmpty()) return cc.emptySeq(this);
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else if(expr.seqType().type instanceof NodeType && !expr.has(Flag.CNS, Flag.NDT)) {
        final int same = ((Checks<Expr>) ex -> ex.equals(expr)).index(list);
        // identical to first operand: return empty sequence
        // example: text() except text()  ->  ()
        if(same == 0) return cc.emptySeq(this);
        // identical to subsequent operand: remove duplicate
        // example: node() except * except *  ->  node() except *
        if(same > 0) cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
        else list.add(expr);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();

    // ensure that results are always sorted
    switch(exprs.length) {
      case 0:  return Empty.VALUE;
      case 1:  return iterative ? exprs[0] : cc.function(Function._UTIL_DDO, info, exprs[0]);
      default: return this;
    }
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder nodes = new ANodeBuilder();
    Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) nodes.add(toNode(item));
    nodes.ddo();

    final int el = exprs.length;
    for(int e = 1; e < el && !nodes.isEmpty(); e++) {
      iter = exprs[e].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) nodes.removeAll(toNode(item));
    }
    return nodes.value(this);
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
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Except ex = new Except(info, copyAll(cc, vm, exprs));
    ex.iterative = iterative;
    return copyType(ex);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Except && super.equals(obj);
  }
}
