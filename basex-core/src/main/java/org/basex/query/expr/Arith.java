package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.func.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  public final Calc calc;
  /** Optimized calculation. */
  private CalcOpt calcOpt;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param calc calculation operator
   */
  public Arith(final InputInfo info, final Expr expr1, final Expr expr2, final Calc calc) {
    super(info, SeqType.ANY_ATOMIC_TYPE_ZO, expr1, expr2);
    this.calc = calc;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.NUMBER, cc);
    if(allAreValues(false)) return cc.preEval(this);

    // move values to second position
    Expr expr1 = exprs[0], expr2 = exprs[1];
    if((calc == Calc.ADD || calc == Calc.MULTIPLY) && expr1 instanceof Value &&
        !(expr2 instanceof Value)) {
      cc.info(OPTSWAP_X, this);
      exprs[0] = expr2;
      exprs[1] = expr1;
      expr1 = exprs[0];
      expr2 = exprs[1];
    }

    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Type type1 = st1.type, type2 = st2.type;
    final boolean nums = type1.isNumberOrUntyped() && type2.isNumberOrUntyped();

    final Type type = calc.type(type1, type2);
    final boolean noarray = !st1.mayBeArray() && !st2.mayBeArray();
    final boolean one = noarray && st1.oneOrMore() && st2.oneOrMore();
    exprType.assign(type, one ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE);

    Expr expr = emptyExpr();
    // 0 - $x  ->  -$x
    if(expr == this && expr1 == Int.ZERO && calc == Calc.SUBTRACT) {
      expr = new Unary(info, expr2, true).optimize(cc);
    }
    // count($n/@*) + count($n/*)  ->  count(($n/@*, $n/*))
    if(expr == this && Function.COUNT.is(expr1) && calc == Calc.ADD && Function.COUNT.is(expr2)) {
      expr = cc.function(Function.COUNT, info, List.get(cc, info, expr1.arg(0), expr2.arg(0)));
    }
    if(expr == this && nums && noarray && st1.one() && st2.one()) {
      // example: number($a) + 0  ->  number($a)
      final Expr ex = calc.optimize(expr1, expr2, info, cc);
      if(ex != null) {
        expr = ex;
      } else if(expr1 instanceof Arith) {
        final Calc acalc = ((Arith) expr1).calc;
        final boolean add = acalc.oneOf(Calc.ADD, Calc.MULTIPLY);
        final boolean sub = acalc.oneOf(Calc.SUBTRACT, Calc.DIVIDE);
        final boolean inverse = acalc == calc.invert();
        final Expr arg1 = expr1.arg(0), arg2 = expr1.arg(1);

        if(arg2 instanceof Value && expr2 instanceof Value && (acalc == calc || inverse)) {
          // (E - 3) + 2  ->  E - (3 - 2)
          // (E * 3 div 2  ->  E * (3 div 2)
          final Calc ncalc = add ? calc : sub ? calc.invert() : null;
          if(ncalc != null) {
            expr = new Arith(info, arg1, new Arith(info, arg2, expr2, ncalc).optimize(cc),
                acalc).optimize(cc);
          }
        } else if(inverse) {
          // E + NUMBER - NUMBER  ->  E
          // NUMBER * E div NUMBER  ->  E
          expr = arg2.equals(expr2) ? arg1 : arg1.equals(expr2) && add ? arg2 : this;
          if(expr != this) expr = new Cast(cc.sc(), info, expr, SeqType.NUMERIC_O).optimize(cc);
        }
      }
    }

    if(expr == this) calcOpt = CalcOpt.get(st1, st2, calc);
    return cc.replaceWith(this, expr);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(calcOpt != null) return calcOpt.eval(exprs[0].item(qc, info), exprs[1].item(qc, info), info);

    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1.isEmpty()) return Empty.VALUE;
    final Item item2 = exprs[1].atomItem(qc, info);
    return item2.isEmpty() ? Empty.VALUE : calc.eval(item1, item2, info);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.PREDICATE && Function.LAST.is(exprs[0]) && exprs[1] instanceof ANum) {
      // E[last() + 1]  ->  E[false()]
      final double d = ((ANum) exprs[1]).dbl();
      if(calc == Calc.ADD && d > 0 || calc == Calc.SUBTRACT && d < 0 ||
        calc == Calc.MULTIPLY && d > 1 || calc == Calc.DIVIDE && d < 1) expr = Bln.FALSE;
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Arith copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Arith arith = new Arith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), calc);
    arith.calcOpt = calcOpt;
    return copyType(arith);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Arith && calc == ((Arith) obj).calc && super.equals(obj);
  }

  @Override
  public String description() {
    return '\'' + calc.name + "' expression";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, OP, calc.name), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, ' ' + calc.name + ' ', true);
  }
}
