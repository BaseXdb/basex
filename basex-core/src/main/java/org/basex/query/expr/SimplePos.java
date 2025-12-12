package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple position range check.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SimplePos extends Arr implements CmpPos {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs single or min/max expressions
   */
  SimplePos(final InputInfo info, final Expr... exprs) {
    super(info, Types.BOOLEAN_O, exprs);
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
      if(expr instanceof final ANum num && !(num instanceof Itr)) {
        final long p = num.itr();
        if(p == num.dbl()) return Itr.get(p);
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
    if(ex == null && exprs[0] instanceof final Itr itr1) {
      final long mn = itr1.itr();
      if(exact()) {
        ex = IntPos.get(mn, mn, info);
      } else if(exprs[1] instanceof final Itr itr2) {
        ex = IntPos.get(mn, itr2.itr(), info);
      }
    }
    return ex != null ? cc.replaceWith(this, ex) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    ctxValue(qc);

    final Item min = exprs[0].atomItem(qc, info);
    if(min.isEmpty()) return false;

    final long p = qc.focus.pos;
    if(exact()) return p == toDouble(min);

    final Item max = exprs[1].atomItem(qc, info);
    if(max.isEmpty()) return false;

    return p >= toDouble(min) && p <= toDouble(max);
  }

  @Override
  public Value positions(final QueryContext qc) throws QueryException {
    final Item min = exprs[0].atomItem(qc, info);
    if(min.isEmpty()) return Empty.VALUE;

    final Item max = exact() ? min : exprs[1].atomItem(qc, info);
    if(max.isEmpty()) return Empty.VALUE;

    final long mn = (long) Math.ceil(toDouble(min)), mx = (long) Math.floor(toDouble(max));
    return RangeSeq.get(mn, mx - mn + 1, true);
  }

  @Override
  public SimplePos copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new SimplePos(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public Expr mergeEbv(final Expr ex, final boolean or, final CompileContext cc)
      throws QueryException {

    if(!or && ex instanceof final SimplePos pos) {
      final Expr expr1 = exprs[0], expr2 = exact() ? expr1 : exprs[1];
      final Expr pexpr1 = pos.exprs[0], pexpr2 = pos.exact() ? pexpr1 : pos.exprs[1];

      // create intersection: pos: 1, 8 and pos: 6, INF → pos: 6, 8
      final Expr min = expr1 == Itr.ONE ? pexpr1 : pexpr1 == Itr.ONE ? expr1 : null;
      final Expr max = expr2 == Itr.MAX ? pexpr2 : pexpr2 == Itr.MAX ? expr2 : null;
      if(min != null && max != null) return get(min, max, info).optimize(cc);
      // create intersection: pos: 5 and pos: 5, 10 → pos: 5
      // create intersection: pos: 4, 6 and pos: 6 → pos: 6
      if(exact() && (expr1.equals(pexpr1) || expr1.equals(pexpr2))) return this;
      if(pos.exact() && (pexpr1.equals(expr1) || pexpr2.equals(expr2))) return pos;
    }
    return null;
  }

  @Override
  public Expr invert(final CompileContext cc) throws QueryException {
    if(exprs[0].seqType().one()) {
      final QuerySupplier<Expr> pos = () -> cc.function(Function.POSITION, info);
      if(exact()) return new CmpG(info, pos.get(), exprs[0], OpG.NE).optimize(cc);
      if(exprs[0] == Itr.ONE) return new CmpG(info, pos.get(), exprs[1], OpG.GT).optimize(cc);
      if(exprs[1] == Itr.MAX) return new CmpG(info, pos.get(), exprs[0], OpG.LT).optimize(cc);
    }
    return null;
  }

  @Override
  public boolean exact() {
    return exprs.length == 1;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.POS.oneOf(flags) || Flag.CTX.oneOf(flags) || super.has(flags);
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
    plan.add(plan.create(this), exact() ? new Expr[] { exprs[0] } : exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.function(Function.POSITION).token("=").token(exprs[0]);
    if(!exact()) qs.token(TO).token(exprs[1]);
  }
}
