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
 * Simple position range check.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
final class SimplePos extends Arr implements CmpPos {
  /**
   * Constructor.
   * @param info input info
   * @param min min expression
   * @param max max expression
   */
  private SimplePos(final InputInfo info, final Expr min, final Expr max) {
    super(info, SeqType.BOOLEAN_O, min, max);
  }

  /**
   * Tries to rewrite {@code fn:position() CMP number(s)} to this expression.
   * @param pos positions to be matched
   * @param op comparison operator
   * @param ii input info
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  static Expr get(final Expr pos, final OpV op, final InputInfo ii, final CompileContext cc)
      throws QueryException {
    final Expr[] minMax = Pos.minMax(pos, op, cc, ii);
    return minMax != null ? new SimplePos(ii, minMax[0], minMax[1]) : null;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    simplifyAll(Simplify.NUMBER, cc);

    Expr min = exprs[0], max = exprs[1], ex = null;
    if(exact()) {
      min = max = min.optimizePos(OpV.EQ, cc);
      if(min instanceof Bln) ex = min;
    }
    if(ex == null && min instanceof Int && max instanceof Int) {
      ex = IntPos.get(((Int) min).itr(), ((Int) max).itr(), info);
    }
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Bln.get(test(qc.focus.pos, qc) != 0);
  }

  @Override
  public SimplePos copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr min = exprs[0].copy(cc, vm), max = exact() ? min : exprs[1].copy(cc, vm);
    return copyType(new SimplePos(info, min, max));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {
    if(or || !(ex instanceof SimplePos)) return null;
    final SimplePos simplePos = (SimplePos) ex;
    final Expr[] posExpr = simplePos.exprs;
    if(!exact() && !simplePos.exact()) {
      final Expr expr1 = exprs[0], expr2 = exprs[1];
      final Expr min = expr1 == Int.ONE ? posExpr[0] : posExpr[0] == Int.ONE ? expr1 : null;
      final Expr max = expr2 == Int.MAX ? posExpr[1] : posExpr[1] == Int.MAX ? expr2 : null;
      if(min != null && max != null) return new SimplePos(info, min, max).optimize(cc);
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
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    if(mode.oneOf(Simplify.PREDICATE)) {
      return exact() && exprs[0].seqType().instanceOf(SeqType.NUMERIC_O) ? exprs[0] : this;
    }
    return super.simplifyFor(mode, cc);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SimplePos && super.equals(obj);
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").token(exprs[0]);
    if(!exact()) qs.token(TO).token(exprs[1]);
  }
}
