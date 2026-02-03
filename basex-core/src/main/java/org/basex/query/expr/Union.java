package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Union extends Set {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public Union(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  Expr opt(final CompileContext cc) throws QueryException {
    flatten(cc);

    // determine type
    SeqType st = SeqType.union(exprs, false);
    if(st == null) st = Types.NODE_ZM;
    if(!(st.type instanceof NodeType)) return null;

    exprType.assign(st.union(Occ.ONE_OR_MORE)).data(exprs);

    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE || list.contains(expr) && !expr.has(Flag.CNS, Flag.NDT)) {
        // remove empty operands: * union () → *
        // remove duplicates: * union * → *
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
    return null;
  }

  @Override
  Or mergePredicates(final Expr[] preds, final CompileContext cc) {
    // *[A] union *[B] → *[A or B]
    return new Or(info, preds);
  }

  @Override
  Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder nodes = new ANodeBuilder();
    for(final Expr expr : exprs) {
      final Iter iter = expr.iter(qc);
      for(Item item; (item = qc.next(iter)) != null;) {
        nodes.add(toNode(item));
      }
    }
    return nodes.value(this);
  }

  @Override
  NodeIter iterate(final QueryContext qc) throws QueryException {
    return new SetIter(qc, iters(qc)) {
      @Override
      public XNode next() throws QueryException {
        if(nodes == null) {
          final int il = iter.length;
          nodes = new XNode[il];
          for(int i = 0; i < il; i++) next(i);
        }

        int m = -1;
        final int il = nodes.length;
        for(int i = 0; i < il; i++) {
          if(nodes[i] == null) continue;
          final int d = m == -1 ? 1 : nodes[m].compare(nodes[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;

        final XNode node = nodes[m];
        next(m);
        return node;
      }
    };
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    for(final Expr expr : exprs) {
      final Item item = expr.iter(qc).next();
      if(item != null) {
        toNode(item);
        return true;
      }
    }
    return false;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      for(final Expr ex : exprs) {
        // boolean(a union <a/>) → boolean(true())
        if(ex.seqType().instanceOf(Types.NODE_OM) && !expr.has(Flag.NDT)) expr = Bln.TRUE;
      }
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Union un = new Union(info, copyAll(cc, vm, exprs));
    un.iterative = iterative;
    return copyType(un);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Union && super.equals(obj);
  }
}
