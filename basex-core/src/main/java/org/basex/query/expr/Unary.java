package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Unary extends Single {
  /** Minus flag. */
  private final boolean minus;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param minus minus flag
   */
  public Unary(final InputInfo info, final Expr expr, final boolean minus) {
    super(info, expr, SeqType.NUMERIC_ZO);
    this.minus = minus;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.NUMBER, cc);

    // no negation, numeric value: return operand
    final SeqType st = expr.seqType();
    final Type type = st.type.isUntyped() ? AtomType.DOUBLE :
      st.type.instanceOf(AtomType.INTEGER) ? AtomType.INTEGER : st.type;
    final Occ occ = st.oneOrMore() && !st.mayBeArray() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
    exprType.assign(type, occ);

    // --123  ->  123
    // --$byte  ->  xs:byte($byte)
    if(!minus && st.instanceOf(SeqType.NUMERIC_ZO)) {
      final Expr cast = new Cast(cc.sc(), info, expr, SeqType.get(type, st.occ)).optimize(cc);
      return cc.replaceWith(this, cast);
    }

    return super.optimize(cc);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = expr.atomItem(qc, info);
    if(item == Empty.VALUE) return Empty.VALUE;

    final Type type = item.type;
    if(type.isUntyped()) {
      final double d = item.dbl(info);
      return Dbl.get(minus ? -d : d);
    }
    if(!type.isNumber()) throw numberError(this, item);

    if(!minus) return item;
    if(type == AtomType.DOUBLE) return Dbl.get(-item.dbl(info));
    if(type == AtomType.FLOAT) return Flt.get(-item.flt(info));
    if(type == AtomType.DECIMAL) return Dec.get(item.dec(info).negate());
    // default: integer
    final long l = item.itr(info);
    if(l == Long.MIN_VALUE) throw RANGE_X.get(info, item);
    return Int.get(-l);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Unary(info, expr.copy(cc, vm), minus));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Unary && minus == ((Unary) obj).minus && super.equals(obj);
  }

  @Override
  public String description() {
    return "unary expression";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, VALUEE, minus), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    if(minus) qs.token("-");
    qs.token(expr);
  }
}
