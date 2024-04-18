package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Value map expression.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class ValueMap extends Mapping {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public ValueMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs[exprs.length - 1].seqType(), exprs);
  }

  /**
   * Creates a new, optimized map expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new CachedValueMap(info, exprs).optimize(cc) : exprs[0];
  }

  @Override
  final boolean items() {
    return false;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    final Expr ex = flattenMaps(cc);
    if(ex != null) return ex;
    final Expr[] merged = merge(cc);
    if(merged != null) return get(cc, info, merged);

    final int el = exprs.length;
    exprType.assign(exprs[el - 1]);

    // use faster implementation for single items
    if(!(this instanceof SingleValueMap)) {
      int e = -1;
      while(++e < el) {
        final SeqType st = exprs[e].seqType();
        if(exprs[e].has(Flag.POS) || !(e < el - 1 ? st.one() : st.zeroOrOne())) break;
      }
      if(e == el) return copyType(new SingleValueMap(info, exprs));
    }
    return this;
  }

  /**
   * Flattens nested map expressions.
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr flattenMaps(final CompileContext cc) throws QueryException {
    // EXPR1 ~ ((ITEM1 ! ITEM2) ~ EXPR2)  ->  EXPR1 ~ ITEM1 ~ ITEM2 ~ EXPR2
    final ExprList list = new ExprList();
    for(final Expr expr : exprs) {
      if(expr instanceof ValueMap || expr instanceof ItemMap) {
        list.add(expr.args());
        cc.info(OPTFLAT_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    return list.size() != exprs.length ? get(cc, info, list.finish()) : null;
  }

  @Override
  final Expr merge(final Expr expr, final Expr next, final CompileContext cc)
      throws QueryException {
    if(!expr.has(Flag.NDT)) {
      if(!next.has(Flag.CTX)) return next;
      return inline(expr, next, cc);
    }
    return null;
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof ValueMap && super.equals(obj);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.tokens(exprs, " ~ ", true);
  }
}
