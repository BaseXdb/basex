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

    Expr min = null, max = null;
    if(arg.isSimple()) {
      if(arg instanceof Range && cmp == OpV.EQ) {
        final Range r = (Range) arg;
        final Expr e1 = r.exprs[0], e2 = r.exprs[1];
        if(e1.seqType().type.instanceOf(AtomType.ITR) &&
           e2.seqType().type.instanceOf(AtomType.ITR)) {
          min = e1;
          max = e1.sameAs(e2) ? e1 : e2;
        }
      } else {
        final SeqType st = arg.seqType();
        if(st.oneNoArray()) {
          switch(cmp) {
            case EQ:
              min = arg;
              max = arg;
              break;
            case GE:
              min = arg;
              max = Int.MAX;
              break;
            case GT:
              min = new Arith(ii, st.type.instanceOf(AtomType.ITR) ? arg :
                cc.function(Function.FLOOR, ii, arg), Int.ONE, Calc.PLUS);
              max = Int.MAX;
              break;
            case LE:
              min = Int.ONE;
              max = arg;
              break;
            case LT:
              min = Int.ONE;
              max = new Arith(ii, st.type.instanceOf(AtomType.ITR) ? arg :
                cc.function(Function.CEILING, ii, arg), Int.ONE, Calc.MINUS);
              break;
            default:
          }
        }
      }
    }
    return min == null ? orig : new Pos(ii, min, max);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Item it1 = exprs[0].atomItem(qc, info);
    if(it1 == null) return Bln.FALSE;
    final long pos = qc.focus.pos;
    final double s = toDouble(it1);
    if(eq()) return Bln.get(pos == s);

    final Item it2 = exprs[1].atomItem(qc, info);
    if(it2 == null) return Bln.FALSE;
    final double e = toDouble(it2);
    return Bln.get(pos >= s && pos <= e);
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Pos(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm));
  }

  /**
   * Creates an intersection of the existing and the specified position expressions.
   * @param pos second position expression
   * @param ii input info
   * @return resulting expression, or {@code null} if intersection is not possible
   */
  Expr intersect(final Pos pos, final InputInfo ii) {
    final Expr[] r1 = exprs, r2 = pos.exprs;
    if(!eq() && !pos.eq()) {
      final Expr min = r1[0] == Int.ONE ? r2[0] : r2[0] == Int.ONE ? r1[0] : null;
      final Expr max = r1[1] == Int.MAX ? r2[1] : r2[1] == Int.MAX ? r1[1] : null;
      if(min != null && max != null) return new Pos(ii, min, max);
    }
    return null;
  }

  /**
   * Checks if minimum and maximum expressions are identical.
   * @return result of check
   */
  private boolean eq() {
    final Expr[] ex = exprs;
    return ex[0] == ex[1];
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.POS;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Pos && sameAs((Pos) cmp);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("position() = ").append(exprs[0]);
    if(!eq()) sb.append(' ' + TO + ' ').append(exprs[1]);
    return sb.toString();
  }
}
