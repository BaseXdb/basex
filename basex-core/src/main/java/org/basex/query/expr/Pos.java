package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Position check expression.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * Returns an instance of this class, an optimized expression, or {@code null}
   * @param cmp2 position to be compared
   * @param op comparator
   * @param ii input info
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  public static Expr get(final Expr cmp2, final OpV op, final InputInfo ii, final CompileContext cc)
      throws QueryException {

    Expr min = null, max = null;
    final SeqType st2 = cmp2.seqType();

    // rewrite only simple expressions that may be simplified even further later on
    if(cmp2.isSimple()) {
      if(cmp2 instanceof Range && op == OpV.EQ) {
        final Range range = (Range) cmp2;
        final Expr start = range.exprs[0], end = range.exprs[1];
        if(st2.type.instanceOf(AtomType.ITR)) {
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
            min = new Arith(ii, st2.type.instanceOf(AtomType.ITR) ? cmp2 :
              cc.function(Function.FLOOR, ii, cmp2), Int.ONE, Calc.PLUS).optimize(cc);
            max = Int.MAX;
            break;
          case LE:
            min = Int.ONE;
            max = cmp2;
            break;
          case LT:
            min = Int.ONE;
            max = new Arith(ii, st2.type.instanceOf(AtomType.ITR) ? cmp2 :
              cc.function(Function.CEILING, ii, cmp2), Int.ONE, Calc.MINUS).optimize(cc);
            break;
          default:
        }
      } else if(cmp2 == Empty.VALUE) {
        return Bln.FALSE;
      }
    }
    return min != null ? new Pos(ii, min, max) : null;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Bln.FALSE;
    final long pos = qc.focus.pos;
    final double start = toDouble(item1);
    if(eq()) return Bln.get(pos == start);

    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return Bln.FALSE;
    final double end = toDouble(item2);
    return Bln.get(pos >= start && pos <= end);
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Pos(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm));
  }

  @Override
  public Expr merge(final Expr ex, final boolean union, final CompileContext cc) {
    if(union || !(ex instanceof Pos)) return null;
    final Pos pos = (Pos) ex;
    final Expr[] posExpr = pos.exprs;
    if(!eq() && !pos.eq()) {
      final Expr min = exprs[0] == Int.ONE ? posExpr[0] : posExpr[0] == Int.ONE ? exprs[0] : null;
      final Expr max = exprs[1] == Int.MAX ? posExpr[1] : posExpr[1] == Int.MAX ? exprs[1] : null;
      if(min != null && max != null) return new Pos(info, min, max);
    }
    return null;
  }

  /**
   * Checks if minimum and maximum expressions are identical.
   * @return result of check
   */
  public boolean eq() {
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
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Function.POSITION).append(" = ").append(exprs[0]);
    if(!eq()) sb.append(' ' + TO + ' ').append(exprs[1]);
    return sb.toString();
  }
}
