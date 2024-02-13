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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class SimplePos extends Arr implements CmpPos {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs single or min/max expressions
   */
  SimplePos(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.BOOLEAN_O, exprs);
  }

  /**
   * Returns a position expression for the specified range, or an optimized boolean item.
   * @param min minimum position
   * @param max maximum position (inclusive, can be {@code null})
   * @param info input info (can be {@code null})
   * @return expression
   */
  public static Expr get(final Expr min, final Expr max, final InputInfo info) {
    return max == null || min.equals(max) ? new SimplePos(info, min) :
      new SimplePos(info, min, max);
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

    Expr ex = null;
    if(exact()) {
      ex = exprs[0].optimizePos(OpV.EQ, cc);
      if(!(ex instanceof Bln)) ex = null;
    } else {
      exprs[1] = simplify.apply(exprs[1]);
    }
    if(ex == null && exprs[0] instanceof Int) {
      final long mn = ((Int) exprs[0]).itr();
      if(exact()) {
        ex = IntPos.get(mn, mn, info);
      } else if(exprs[1] instanceof Int) {
        ex = IntPos.get(mn, ((Int) exprs[1]).itr(), info);
      }
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
    return copyType(new SimplePos(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    if(!or && ex instanceof SimplePos) {
      final SimplePos pos = (SimplePos) ex;
      final Expr expr1 = exprs[0], expr2 = exact() ? expr1 : exprs[1];
      final Expr pexpr1 = pos.exprs[0], pexpr2 = pos.exact() ? pexpr1 : pos.exprs[1];

      // create intersection: pos: 1, 8 and pos: 6, INF  ->  pos: 6, 8
      final Expr min = expr1 == Int.ONE ? pexpr1 : pexpr1 == Int.ONE ? expr1 : null;
      final Expr max = expr2 == Int.MAX ? pexpr2 : pexpr2 == Int.MAX ? expr2 : null;
      if(min != null && max != null) return SimplePos.get(min, max, info).optimize(cc);
      // create intersection: pos: 5 and pos: 5, 10  ->  pos: 5
      // create intersection: pos: 4, 6 and pos: 6  ->  pos: 6
      if(exact() && (expr1.equals(pexpr1) || expr1.equals(pexpr2))) return this;
      if(pos.exact() && (pexpr1.equals(expr1) || pexpr2.equals(expr2))) return pos;
    }
    return null;
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    if(exprs[0].seqType().one()) {
      final QuerySupplier<Expr> pos = () -> cc.function(Function.POSITION, info);
      if(exact())
        return new CmpG(info, pos.get(), exprs[0], OpG.NE, null, cc.sc()).optimize(cc);
      if(exprs[0] == Int.ONE)
        return new CmpG(info, pos.get(), exprs[1], OpG.GT, null, cc.sc()).optimize(cc);
      if(exprs[1] == Int.MAX)
        return new CmpG(info, pos.get(), exprs[0], OpG.LT, null, cc.sc()).optimize(cc);
    }
    return null;
  }

  @Override
  public boolean exact() {
    return exprs.length == 1;
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
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof SimplePos && super.equals(obj);
  }

  @Override
  public String description() {
    return "positional access";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), exact() ? exprs[0] : exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").token(exprs[0]);
    if(!exact()) qs.token(TO).token(exprs[1]);
  }
}
