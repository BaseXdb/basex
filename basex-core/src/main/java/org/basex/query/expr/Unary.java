package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Unary expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
    final SeqType st = expr.seqType();
    final Type t = st.type;
    exprType.assign(t.isUntyped() ? AtomType.DBL : t.isNumber() ? t : AtomType.ITR,
      st.oneNoArray() ? Occ.ONE : Occ.ZERO_ONE);

    // no negation, numeric value: return operand
    return !minus && st.instanceOf(SeqType.NUM_ZO) ? cc.replaceWith(this, expr) :
      expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = expr.atomItem(qc, info);
    if(it == null) return null;

    final Type ip = it.type;
    if(ip.isUntyped()) {
      final double d = it.dbl(info);
      return Dbl.get(minus ? -d : d);
    }
    if(!ip.isNumber()) throw numberError(this, it);

    if(!minus) return it;
    if(ip == AtomType.DBL) return Dbl.get(-it.dbl(info));
    if(ip == AtomType.FLT) return Flt.get(-it.flt(info));
    if(ip == AtomType.DEC) return Dec.get(it.dec(info).negate());
    // default: integer
    final long l = it.itr(info);
    if(l == Long.MIN_VALUE) throw RANGE_X.get(info, it);
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VALUEE, minus), expr);
  }

  @Override
  public String toString() {
    return (minus ? "-" : "") + expr;
  }
}
