package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

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
 * Intersect expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Intersect extends Set {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Intersect(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  Expr opt(final CompileContext cc) throws QueryException {
    flatten(cc);

    // determine type
    SeqType st = SeqType.NODE_ZM;
    for(final Expr expr : exprs) {
      final SeqType st2 = expr.seqType();
      if(!st2.zero()) {
        if(!(st2.type instanceof NodeType)) return null;
        st = st.intersect(st2);
        // check for incompatible node types
        if(st == null) return Empty.VALUE;
      }
    }
    exprType.assign(st.union(Occ.ZERO)).data(exprs);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE) {
        // empty operand: * intersect ()  ->  ()
        return Empty.VALUE;
      } else if(!expr.has(Flag.CNS, Flag.NDT) && list.contains(expr)) {
        // remove duplicates: * intersect *  ->  *
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();

    final Expr ex = rewrite(Union.class, (invert, ops) ->
      invert ? new Union(info, ops) : new Intersect(info, ops), cc);
    if(ex != null) {
      cc.info(OPTREWRITE_X_X, (Supplier<?>) this::description, ex);
      return ex;
    }
    return null;
  }

  @Override
  And mergePredicates(final Expr[] preds, final CompileContext cc) {
    return new And(info, preds);
  }

  @Override
  Value nodes(final QueryContext qc) throws QueryException {
    ANodeBuilder nodes = new ANodeBuilder();
    Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      nodes.add(toNode(item));
    }

    final int el = exprs.length;
    for(int e = 1; e < el && !nodes.isEmpty(); ++e) {
      nodes.ddo();
      final ANodeBuilder tmp = new ANodeBuilder();
      iter = exprs[e].iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        final ANode node = toNode(item);
        if(nodes.contains(node)) tmp.add(node);
      }
      nodes = tmp;
    }
    return nodes.value(this);
  }

  @Override
  NodeIter iterate(final QueryContext qc) throws QueryException {
    return new SetIter(qc, iters(qc)) {
      @Override
      public ANode next() throws QueryException {
        final int irl = iter.length;
        if(nodes == null) nodes = new ANode[irl];

        for(int i = 0; i < irl; i++) {
          if(!next(i)) return null;
        }

        final int il = nodes.length;
        for(int i = 1; i < il;) {
          final int d = nodes[0].compare(nodes[i]);
          if(d > 0) {
            if(!next(i)) return null;
          } else if(d < 0) {
            if(!next(0)) return null;
            i = 1;
          } else {
            ++i;
          }
        }
        return nodes[0];
      }
    };
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Intersect is = new Intersect(info, copyAll(cc, vm, exprs));
    is.iterative = iterative;
    return copyType(is);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Intersect && super.equals(obj);
  }
}
