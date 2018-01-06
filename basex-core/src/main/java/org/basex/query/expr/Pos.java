package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Position check expression.
 *
 * @author BaseX Team 2005-18, BSD License
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
    super(info, SeqType.BLN_O, min, max);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * Returns an instance of this class, the original expression, or an optimized expression.
   * @param cmp comparison expression
   * @param op comparator
   * @param info input info
   * @param cc compilation context
   * @return resulting or original expression
   * @throws QueryException query exception
   */
  public static Expr get(final Cmp cmp, final OpV op, final InputInfo info,
      final CompileContext cc) throws QueryException {

    final Expr cmp1 = cmp.exprs[0], cmp2 = cmp.exprs[1];
    if(!cmp1.isFunction(Function.POSITION)) return cmp;

    Expr min = null, max = null;
    final SeqType st1 = cmp1.seqType(), st2 = cmp2.seqType();
    if(cmp2.isSimple()) {
      if(cmp2 instanceof Range && op == OpV.EQ) {
        final Range range = (Range) cmp2;
        final Expr start = range.exprs[0], end = range.exprs[1];
        if(st1.type.instanceOf(AtomType.ITR) && st2.type.instanceOf(AtomType.ITR)) {
          min = start;
          max = start.equals(end) ? start : end;
        }
      } else if(st2.oneNoArray()) {
        switch(op) {
          case EQ:
            min = cmp2;
            max = cmp2;
            break;
          case GE:
            min = cmp2;
            max = Int.MAX;
            break;
          case GT:
            min = new Arith(info, st2.type.instanceOf(AtomType.ITR) ? cmp2 :
              cc.function(Function.FLOOR, info, cmp2), Int.ONE, Calc.PLUS);
            max = Int.MAX;
            break;
          case LE:
            min = Int.ONE;
            max = cmp2;
            break;
          case LT:
            min = Int.ONE;
            max = new Arith(info, st2.type.instanceOf(AtomType.ITR) ? cmp2 :
              cc.function(Function.CEILING, info, cmp2), Int.ONE, Calc.MINUS);
            break;
          default:
        }
      }
    }
    return min == null ? cmp : new Pos(info, min, max);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == null) return Bln.FALSE;
    final long pos = qc.focus.pos;
    final double start = toDouble(item1);
    if(eq()) return Bln.get(pos == start);

    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == null) return Bln.FALSE;
    final double end = toDouble(item2);
    return Bln.get(pos >= start && pos <= end);
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
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Pos && super.equals(obj);
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(PAREN1).append("position() = ").append(exprs[0]);
    if(!eq()) sb.append(' ' + TO + ' ').append(exprs[1]);
    return sb.append(PAREN2).toString();
  }
}
