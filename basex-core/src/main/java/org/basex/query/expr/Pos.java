package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
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
 * @author BaseX Team 2005-20, BSD License
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
   * @param expr positions to be matched
   * @param op comparator
   * @param ii input info
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr get(final Expr expr, final OpV op, final InputInfo ii, final CompileContext cc)
      throws QueryException {

    Expr min = null, max = null;
    final SeqType st2 = expr.seqType();

    // rewrite only simple expressions that may be simplified even further later on
    if(expr.isSimple()) {
      if(expr instanceof Range && op == OpV.EQ) {
        final Range range = (Range) expr;
        final Expr start = range.exprs[0], end = range.exprs[1];
        if(st2.type.instanceOf(AtomType.ITR)) {
          min = start;
          max = start.equals(end) ? start : end;
        }
      } else if(st2.one() && !st2.mayBeArray()) {
        switch(op) {
          case EQ:
            min = expr;
            max = expr;
            break;
          case GE:
            min = expr;
            max = Int.MAX;
            break;
          case GT:
            min = new Arith(ii, st2.type.instanceOf(AtomType.ITR) ? expr :
              cc.function(Function.FLOOR, ii, expr), Int.ONE, Calc.PLUS).optimize(cc);
            max = Int.MAX;
            break;
          case LE:
            min = Int.ONE;
            max = expr;
            break;
          case LT:
            min = Int.ONE;
            max = new Arith(ii, st2.type.instanceOf(AtomType.ITR) ? expr :
              cc.function(Function.CEILING, ii, expr), Int.ONE, Calc.MINUS).optimize(cc);
            break;
          default:
        }
      } else if(expr == Empty.VALUE) {
        return Bln.FALSE;
      }
    }
    return min != null ? new Pos(ii, min, max) : null;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.NUMBER, cc);
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);

    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Bln.FALSE;
    final long pos = qc.focus.pos;
    final double start = toDouble(item1);
    if(exact()) return Bln.get(pos == start);

    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return Bln.FALSE;
    final double end = toDouble(item2);
    return Bln.get(pos >= start && pos <= end);
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Pos(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc) {
    if(or || !(ex instanceof Pos)) return null;
    final Pos pos = (Pos) ex;
    final Expr[] posExpr = pos.exprs;
    if(!exact() && !pos.exact()) {
      final Expr min = exprs[0] == Int.ONE ? posExpr[0] : posExpr[0] == Int.ONE ? exprs[0] : null;
      final Expr max = exprs[1] == Int.MAX ? posExpr[1] : posExpr[1] == Int.MAX ? exprs[1] : null;
      if(min != null && max != null) return new Pos(info, min, max);
    }
    return null;
  }

  /**
   * If possible, returns an optimized expression with inverted operands.
   * @param cc compilation context
   * @return original or modified expression
   * @throws QueryException query exception
   */
  public Expr invert(final CompileContext cc) throws QueryException {
    if(exprs[0].seqType().one()) {
      final Expr pos = cc.function(Function.POSITION, info);
      if(exact()) {
        return new CmpG(pos, exprs[0], OpG.NE, null, cc.sc(), info).optimize(cc);
      } else if(exprs[0] == Int.ONE) {
        return new CmpG(pos, exprs[1], OpG.GT, null, cc.sc(), info).optimize(cc);
      } else if(exprs[1] == Int.MAX) {
        return new CmpG(pos, exprs[0], OpG.LT, null, cc.sc(), info).optimize(cc);
      }
    }
    return this;
  }

  /**
   * Checks if minimum and maximum expressions are identical.
   * @return result of check
   */
  boolean exact() {
    return exprs[0] == exprs[1];
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags);
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
  public void plan(final QueryString qs) {
    qs.function(Function.POSITION).token("=").token(exprs[0]);
    if(!exact()) qs.token(TO).token(exprs[1]);
  }
}
