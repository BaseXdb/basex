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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Arith extends Arr {
  /** Calculation operator. */
  public final Calc calc;

  /**
   * Constructor.
   * @param info input info
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
    simplifyAll(Simplify.NUMBER, cc);
    if(allAreValues(false)) return cc.preEval(this);

    // move values to second position
    Expr expr1 = exprs[0], expr2 = exprs[1];
    if((calc == Calc.PLUS || calc == Calc.MULT) && (
        expr1 instanceof Value && !(expr2 instanceof Value))) {
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
    if(expr == this && expr1 == Int.ZERO && calc == Calc.MINUS) {
      expr = new Unary(info, expr2, true).optimize(cc);
    }
    // count($n/@*) + count($n/*)  ->  count(($n/@*, $n/*))
    if(expr == this && Function.COUNT.is(expr1) && calc == Calc.PLUS && Function.COUNT.is(expr2)) {
      expr = cc.function(Function.COUNT, info, List.get(cc, info, expr1.arg(0), expr2.arg(0)));
    }
    if(expr == this && nums && noarray && st1.one() && st2.one()) {
      // example: number($a) + 0  ->  number($a)
      final Expr ex = calc.optimize(expr1, expr2, info, cc);
      if(ex != null) {
        expr = ex;
      } else if(expr1 instanceof Arith) {
        final Calc acalc = ((Arith) expr1).calc;
        final Expr arg1 = expr1.arg(0), arg2 = expr1.arg(1);
        if(arg2 instanceof Value && expr2 instanceof Value &&
            (acalc == calc || acalc == calc.invert())) {
          // (E - 3) + 2  ->  E - (3 - 2)
          // (E * 3 div 2  ->  E * (3 div 2)
          final Calc ncalc = acalc == Calc.PLUS || acalc == Calc.MULT ? calc :
            acalc == Calc.MINUS || acalc == Calc.DIV ? calc.invert() : null;
          if(ncalc != null) {
            expr = new Arith(info, arg1, new Arith(info, arg2, expr2, ncalc).optimize(cc),
                acalc).optimize(cc);
          }
        } else if(acalc == calc.invert() && arg2.equals(expr2)) {
          // E + INT - INT  ->  E
          expr = arg1.seqType().instanceOf(SeqType.NUMERIC_O) ? arg1 :
            new Cast(cc.sc(), info, arg1, SeqType.NUMERIC_O).optimize(cc);
        }
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Empty.VALUE;
    final Item item2 = exprs[1].atomItem(qc, info);
    return item2 == Empty.VALUE ? Empty.VALUE : calc.eval(item1, item2, info);
  }

  @Override
  public Arith copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Arith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), calc));
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
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OP, calc.name), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + calc.name + ' ', true);
  }
}
