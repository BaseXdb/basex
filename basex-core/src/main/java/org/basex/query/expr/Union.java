package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
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
 * Union expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
  Expr opt(final CompileContext cc) throws QueryException {
    flatten(cc);

    // determine type
    SeqType st = null;
    for(final Expr expr : exprs) {
      final SeqType st2 = expr.seqType();
      if(!st2.zero()) st = st == null ? st2 : st.union(st2);
    }
    // check if all operands yield an empty sequence
    if(st == null) st = SeqType.NODE_ZM;

    // skip optimizations if operands do not have the correct type
    if(st.type instanceof NodeType) {
      exprType.assign(st.union(Occ.ONE_OR_MORE));

      final ExprList list = new ExprList(exprs.length);
      for(final Expr expr : exprs) {
        if(expr == Empty.VALUE || list.contains(expr) && !expr.has(Flag.CNS, Flag.NDT)) {
          // remove empty operands: * union ()  ->  *
          // remove duplicates: * union *  ->  *
          cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
        } else {
          list.add(expr);
        }
      }
      exprs = list.finish();

      final Expr ex = rewrite(Intersect.class, (invert, ops) ->
        invert ? new Intersect(info, ops) : new Union(info, ops), cc);
      if(ex != null) {
        cc.info(OPTREWRITE_X_X, (Supplier<?>) this::description, ex);
        return ex;
      }
    }
    return null;
  }

  @Override
  Or mergePredicates(final Expr[] preds, final CompileContext cc) {
    return new Or(info, preds);
  }

  @Override
  Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder nodes = new ANodeBuilder();
    for(final Expr expr : exprs) {
      final Iter iter = expr.iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) nodes.add(toNode(item));
    }
    return nodes.value(this);
  }

  @Override
  NodeIter iterate(final QueryContext qc) throws QueryException {
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
  public Data data() {
    return data(exprs);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Union un = new Union(info, copyAll(cc, vm, exprs));
    un.iterative = iterative;
    return copyType(un);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Union && super.equals(obj);
  }
}
