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
 * Position range check.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Pos extends Arr implements CmpPos {
  /**
   * Constructor.
   * @param info input info
   * @param min min expression
   * @param max max expression
   */
  private Pos(final InputInfo info, final Expr min, final Expr max) {
    super(info, SeqType.BOOLEAN_O, min, max);
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
        if(st2.type.instanceOf(AtomType.INTEGER)) {
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
            min = new Arith(ii, st2.type.instanceOf(AtomType.INTEGER) ? expr :
              cc.function(Function.FLOOR, ii, expr), Int.ONE, Calc.PLUS).optimize(cc);
            max = Int.MAX;
            break;
          case LE:
            min = Int.ONE;
            max = expr;
            break;
          case LT:
            min = Int.ONE;
            max = new Arith(ii, st2.type.instanceOf(AtomType.INTEGER) ? expr :
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

    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 instanceof Int && expr2 instanceof Int) {
      return cc.replaceWith(this, ItrPos.get(((Int) expr1).itr(), ((Int) expr2).itr(), info));
    }
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(test(qc.focus.pos, qc) != 0);
  }

  @Override
  public Pos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr min = exprs[0].copy(cc, vm), max = exact() ? min : exprs[1].copy(cc, vm);
    return copyType(new Pos(info, min, max));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc) {
    if(or || !(ex instanceof Pos)) return null;
    final Pos pos = (Pos) ex;
    final Expr[] posExpr = pos.exprs;
    if(!exact() && !pos.exact()) {
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      final Expr min = expr1 == Int.ONE ? posExpr[0] : posExpr[0] == Int.ONE ? expr1 : null;
      final Expr max = expr2 == Int.MAX ? posExpr[1] : posExpr[1] == Int.MAX ? expr2 : null;
      if(min != null && max != null) return new Pos(info, min, max);
    }
    return null;
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    if(exprs[0].seqType().one()) {
      final Expr pos = cc.function(Function.POSITION, info);
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      if(exact()) {
        return new CmpG(pos, expr1, OpG.NE, null, cc.sc(), info).optimize(cc);
      } else if(expr1 == Int.ONE) {
        return new CmpG(pos, expr2, OpG.GT, null, cc.sc(), info).optimize(cc);
      } else if(expr2 == Int.MAX) {
        return new CmpG(pos, expr1, OpG.LT, null, cc.sc(), info).optimize(cc);
      }
    }
    return this;
  }

  @Override
  public boolean exact() {
    return exprs[0] == exprs[1];
  }

  @Override
  public boolean simple() {
    return exprs[0].isSimple() && (exact() || exprs[1].isSimple());
  }

  @Override
  public int test(final long pos, final QueryContext qc) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return 0;
    final double min = toDouble(item1), max;
    if(exact()) {
      max = min;
    } else {
      final Item item2 = exprs[1].atomItem(qc, info);
      if(item2 == Empty.VALUE) return 0;
      max = toDouble(item2);
    }
    return pos == max ? 2 : pos >= min && pos <= max ? 1 : 0;
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
