package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
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
 * Arithmetic expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  public final Calc calc;
  /** Optimized calculation (can be {@code null}). */
  private CalcOpt calcOpt;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param calc calculation operator
   */
  public Arith(final InputInfo info, final Expr expr1, final Expr expr2, final Calc calc) {
    super(info, Types.ANY_ATOMIC_TYPE_ZO, expr1, expr2);
    this.calc = calc;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.NUMBER, cc);
    if(values(false, cc)) return cc.preEval(this);

    // move values to second position
    // 1 + position() → position() + 1
    Expr expr1 = exprs[0], expr2 = exprs[1];
    if((calc == Calc.ADD || calc == Calc.MULTIPLY) && expr1 instanceof Value &&
        expr1.seqType().type.isNumberOrUntyped() && !(expr2 instanceof Value)) {
      cc.info(OPTSWAP_X, this);
      exprs[0] = expr2;
      exprs[1] = expr1;
      expr1 = exprs[0];
      expr2 = exprs[1];
    }

    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Type type1 = st1.type, type2 = st2.type;
    final boolean numbers = type1.isNumberOrUntyped() && type2.isNumberOrUntyped();

    final Type type = calc.type(type1, type2);
    final boolean noArrays = !st1.mayBeWrapped() && !st2.mayBeWrapped();
    final boolean oneOrMore = noArrays && st1.oneOrMore() && st2.oneOrMore();
    exprType.assign(type, oneOrMore ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE);

    Expr expr = emptyExpr();
    // 0 - $x → -$x; ignore floating numbers (0 - 0e0 → 0e0)
    if(expr == this && expr1 == Itr.ZERO && calc == Calc.SUBTRACT &&
        type2.instanceOf(BasicType.DECIMAL)) {
      expr = new Unary(info, expr2, true).optimize(cc);
    }
    // count($n/@*) + count($n/*) → count(($n/@*, $n/*))
    if(expr == this && Function.COUNT.is(expr1) && calc == Calc.ADD && Function.COUNT.is(expr2)) {
      expr = cc.function(Function.COUNT, info, List.get(cc, info, expr1.arg(0), expr2.arg(0)));
    }
    // sum(E) div count(E) → avg(E)
    if(expr == this && Function.SUM.is(expr1) && expr1.args().length == 1 &&
        calc == Calc.DIVIDE && Function.COUNT.is(expr2) && expr1.arg(0).equals(expr2.arg(0)) &&
        !has(Flag.NDT)) {
      expr = cc.function(Function.AVG, info, expr1.arg(0));
    }
    if(expr == this && numbers && noArrays && st1.one() && st2.one() && !has(Flag.NDT)) {
      // nondeterministic operands must not be dropped, in order to preserve side effects
      // ($x + 1) + ($y - 1) → $x + $y
      Expr ex = Linear.normalize(this, cc);
      // number($a) + 0 → number($a)
      if(ex == null) ex = calc.optimize(expr1, expr2, info, cc);
      if(ex != null) {
        expr = ex;
      } else if(expr1 instanceof final Arith arth1 && arth1.calc == Calc.MULTIPLY &&
          type1.instanceOf(BasicType.DECIMAL) && type2.instanceOf(BasicType.DECIMAL)) {
        // exact arithmetic only: no floating-point numbers
        final Expr arg1 = arth1.arg(0), arg2 = arth1.arg(1);
        if(calc == Calc.MULTIPLY && arg2 instanceof ANum && expr2 instanceof ANum) {
          // E * 3 * 2 → E * (3 * 2)
          expr = new Arith(info, arg1, new Arith(info, arg2, expr2, calc).optimize(cc),
              calc).optimize(cc);
        } else if(calc == Calc.DIVIDE) {
          // NUMBER * E div NUMBER → E
          final Expr arg = arg2.equals(expr2) ? arg1 : arg1.equals(expr2) ? arg2 : null;
          final Expr cast = arg != null ? Calc.cast(arg, type, true, info, cc) : null;
          if(cast != null) expr = cast;
        }
      } else if(calc.oneOf(Calc.ADD, Calc.SUBTRACT) && expr2 instanceof final ANum num &&
          num.dbl() < 0) {
        // E + -1 → E - 1
        // E - -1 → E + 1
        final Expr inv2 = new Unary(info, num, true).optimize(cc);
        expr = new Arith(info, expr1, inv2, calc.invert()).optimize(cc);
      }
    }

    if(expr == this) calcOpt = CalcOpt.get(st1, st2, calc);
    return cc.replaceWith(this, expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // optimized calculation: operands are numbers or untyped, no atomization required
    final boolean opt = calcOpt != null;
    final Item item1 = opt ? exprs[0].item(qc, info) : exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Empty.VALUE;
    final Item item2 = opt ? exprs[1].item(qc, info) : exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return Empty.VALUE;
    return opt ? calcOpt.eval(item1, item2, info) : calc.eval(item1, item2, info);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.PREDICATE && Function.LAST.is(exprs[0]) &&
        exprs[1] instanceof final ANum num) {
      // E[last() + 1] → E[false()]
      final double d = num.dbl();
      if(calc == Calc.ADD && d > 0 || calc == Calc.SUBTRACT && d < 0 ||
        calc == Calc.MULTIPLY && d > 1 || calc == Calc.DIVIDE && d < 1) expr = Bln.FALSE;
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Arith copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Arith arith = new Arith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), calc);
    arith.calcOpt = calcOpt;
    return copyType(arith);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Arith arth && calc == arth.calc && super.equals(obj);
  }

  @Override
  public String description() {
    return '\'' + calc.name + "' calculation";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, OP, calc.name), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.operands(exprs, ' ' + calc.name + ' ');
  }
}
