package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-19, BSD License
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
    super(info, expr, SeqType.NUM_ZO);
    this.minus = minus;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = cc.simplifyAtom(expr);

    final SeqType st = expr.seqType();
    final Type type = st.type;
    exprType.assign(type.isUntyped() ? AtomType.DBL : type.isNumber() ? type : AtomType.ITR,
      st.oneNoArray() ? Occ.ONE : Occ.ZERO_ONE);

    // no negation, numeric value: return operand
    return !minus && st.instanceOf(SeqType.NUM_ZO) ? cc.replaceWith(this, expr) :
      super.optimize(cc);
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
    if(type == AtomType.DBL) return Dbl.get(-item.dbl(info));
    if(type == AtomType.FLT) return Flt.get(-item.flt(info));
    if(type == AtomType.DEC) return Dec.get(item.dec(info).negate());
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
  public String toString() {
    return (minus ? "-" : "") + expr;
  }
}
