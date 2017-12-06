package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Sort key.
 *
 * @author BaseX Team 2005-17, BSD License
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
    return new OrderKey(info, expr.copy(cc, vm), desc, least, coll);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
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
  public void plan(final FElem plan) {
    final FElem elem = planElem(DIR, Token.token(desc ? DESCENDING : ASCENDING),
        Token.token(EMPTYORD), Token.token(least ? LEAST : GREATEST));
    expr.plan(elem);
    plan.add(elem);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(expr.toString());
    if(desc) tb.add(' ').add(DESCENDING);
    tb.add(' ').add(EMPTYORD).add(' ').add(least ? LEAST : GREATEST);
    if(coll != null) tb.add(' ').add(COLLATION).add(" \"").add(coll.uri()).add('"');
    return tb.toString();
  }
}