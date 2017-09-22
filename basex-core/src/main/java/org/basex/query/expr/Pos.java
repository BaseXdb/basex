package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Position check expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Pos extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param min min expression
   * @param max max expression
   */
  private Pos(final InputInfo info, final Expr min, final Expr max) {
    super(info, min, max);
    seqType = SeqType.BLN;
  }

  /**
   * Returns an instance of this class, the original expression, or an optimized expression.
   * @param cmp comparator
   * @param arg argument
   * @param orig original expression
   * @param cc compilation context
   * @param ii input info
   * @return resulting or original expression
   * @throws QueryException query exception
   */
  public static Expr get(final OpV cmp, final Expr arg, final Expr orig, final InputInfo ii,
      final CompileContext cc) throws QueryException {

    if(arg.isSimple()) {
      if(arg instanceof Range && cmp == OpV.EQ) {
        final Range r = (Range) arg;
        final Expr e1 = r.exprs[0], e2 = r.exprs[1];
        if(e1.seqType().type.instanceOf(AtomType.ITR) && e2.seqType().type.instanceOf(AtomType.ITR))
          return new Pos(ii, e1, e1.sameAs(e2) ? e1 : e2);
      }
      final SeqType st = arg.seqType();
      if(st.oneNoArray()) {
        switch(cmp) {
          case EQ:
            return new Pos(ii, arg, arg);
          case GE:
            return new Pos(ii, arg, Int.MAX);
          case GT:
            Expr e = new Arith(ii,
                arg.seqType().type.instanceOf(AtomType.ITR) ? arg :
                cc.function(Function.FLOOR, ii, arg), Int.ONE, Calc.PLUS);
            return new Pos(ii, e, Int.MAX);
          case LE:
            return new Pos(ii, Int.ONE, arg);
          case LT:
            e = new Arith(ii,
                arg.seqType().type.instanceOf(AtomType.ITR) ? arg :
                cc.function(Function.CEILING, ii, arg), Int.ONE, Calc.MINUS);
            return new Pos(ii, Int.ONE, e);
          default:
        }
      }
    }
    return orig;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    final long pos = qc.focus.pos;
    final Item it1 = exprs[0].atomItem(qc, info);
    if(it1 == null) return Bln.FALSE;
    final double s = toDouble(it1);
    if(exprs[0] == exprs[1]) return Bln.get(pos == s);

    final Item it2 = exprs[1].atomItem(qc, info);
    if(it2 == null) return Bln.FALSE;
    final double e = toDouble(it2);
    return Bln.get(pos >= s && pos <= e);
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Pos(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm));
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.POS;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Pos)) return false;
    final Pos p = (Pos) cmp;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      if(!exprs[e].sameAs(p.exprs[e])) return false;
    }
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("position() = ").append(exprs[0]);
    if(exprs[0] != exprs[1]) sb.append(' ' + TO + ' ').append(exprs[1]);
    return sb.toString();
  }
}
