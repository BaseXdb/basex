package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-14, BSD License
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
    super(info, expr);
    this.minus = minus;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    if(expr.isValue()) return preEval(qc);

    final SeqType st = expr.seqType();
    final Type t = st.type;
    seqType = SeqType.get(t.isUntyped() ? AtomType.DBL : t.isNumber() ? t : AtomType.ITR,
      st.one() && !st.mayBeArray() ? Occ.ONE : Occ.ZERO_ONE);
    return this;
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
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new Unary(info, expr.copy(qc, scp, vs), minus));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, minus), expr);
  }

  @Override
  public String toString() {
    return (minus ? "-" : "") + expr;
  }
}
