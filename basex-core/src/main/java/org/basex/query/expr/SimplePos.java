package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple position range check.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class SimplePos extends Arr implements CmpPos {
  /**
   * Constructor.
   * @param info input info
   * @param range (min/max) expressions
   */
  SimplePos(final InputInfo info, final Expr... range) {
    super(info, SeqType.BOOLEAN_O, range);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.NUMBER, cc);

    final QueryFunction<Expr, Expr> simplify = expr -> {
      if(expr instanceof ANum && !(expr instanceof Int)) {
        final ANum num = (ANum) expr;
        final long p = num.itr();
        if(p == num.dbl()) return Int.get(p);
      }
      return expr;
    };
    exprs[0] = simplify.apply(exprs[0]);
    exprs[1] = simplify.apply(exprs[1]);

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
        return new CmpG(info, pos, expr1, OpG.NE, null, cc.sc()).optimize(cc);
      } else if(expr1 == Int.ONE) {
        return new CmpG(info, pos, expr2, OpG.GT, null, cc.sc()).optimize(cc);
      } else if(expr2 == Int.MAX) {
        return new CmpG(info, pos, expr1, OpG.LT, null, cc.sc()).optimize(cc);
      }
    }
    return null;
  }

  @Override
  public boolean exact() {
    return exprs[0] == exprs[1];
  }

  @Override
  public int test(final long pos, final QueryContext qc) throws QueryException {
    final Item min = exprs[0].atomItem(qc, info);
    if(min.isEmpty()) return 0;
    final double mn = toDouble(min), mx;
    if(exact()) {
      mx = mn;
    } else {
      final Item max = exprs[1].atomItem(qc, info);
      if(max.isEmpty()) return 0;
      mx = toDouble(max);
    }
    return pos == mx ? 2 : pos >= mn && pos <= mx ? 1 : 0;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.in(flags) || Flag.CTX.in(flags) || super.has(flags);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[position() = NUMBER]  ->  E[NUMBER]
    return cc.simplify(this, mode == Simplify.PREDICATE && exact() &&
        exprs[0].seqType().instanceOf(SeqType.NUMERIC_O) ? exprs[0] : this, mode);
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
