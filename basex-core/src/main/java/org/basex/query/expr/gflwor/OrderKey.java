package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Sort key.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class OrderKey extends Single {
  /** Descending order flag. */
  final boolean desc;
  /** Position of empty sort keys. */
  final boolean least;
  /** Collation (can be {@code null}). */
  final Collation coll;

  /**
   * Constructor.
   * @param info input info
   * @param key sort key expression
   * @param desc descending order
   * @param least empty least
   * @param coll collation (can be {@code null})
   */
  public OrderKey(final InputInfo info, final Expr key, final boolean desc, final boolean least,
      final Collation coll) {
    super(info, key, SeqType.ITEM_ZM);
    this.desc = desc;
    this.least = least;
    this.coll = coll;
  }

  @Override
  public OrderKey copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new OrderKey(info, expr.copy(cc, vm), desc, least, coll));
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.DATA, cc);
    // override pre-evaluation
    return this;
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof OrderKey)) return false;
    final OrderKey k = (OrderKey) obj;
    return desc == k.desc && least == k.least && Objects.equals(coll, k.coll) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, DIR, Token.token(desc ? DESCENDING : ASCENDING),
        Token.token(EMPTYY), Token.token(least ? LEAST : GREATEST)), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(expr);
    if(desc) qs.token(DESCENDING);
    qs.token(EMPTYY).token(least ? LEAST : GREATEST);
    if(coll != null) qs.token(COLLATION).token("\"").token(coll.uri()).token('"');
  }
}