package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Numeric function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class NumericFn extends StandardFunc {
  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    final Expr input = arg(0);
    if(Function.ABS.is(this)) {
      // abs(abs(E)) → abs(E)
      if(Function.ABS.is(input)) return input;
      // abs(-E) → abs(E)
      if(input instanceof final Unary unary && unary.minus) {
        return cc.function(Function.ABS, info, unary.expr);
      }
    } else if(!defined(1) || arg(1) instanceof final Itr itr && itr.itr() >= 0) {
      // floor(ceiling(E)) → ceiling(E), round($integer) → $integer
      final SeqType ist = input.seqType();
      if(integral(input) || ist.type == BasicType.INTEGER && ist.zeroOrOne()) return input;
    }

    final SeqType st = optType(input);
    if(st != null) exprType.assign(st);
    return this;
  }

  /**
   * Checks if an expression is a function call that returns integral values.
   * @param expr expression
   * @return result of check
   */
  private static boolean integral(final Expr expr) {
    return Function.FLOOR.is(expr) || Function.CEILING.is(expr) ||
      (Function.ROUND.is(expr) || Function.ROUND_HALF_TO_EVEN.is(expr)) &&
      !((NumericFn) expr).defined(1);
  }

  /**
   * Returns a numeric type for the specified type.
   * @param expr expression
   * @return sequence type or {@code null}
   */
  protected static SeqType optType(final Expr expr) {
    final SeqType st = expr.seqType();
    Type type = st.type;
    // untyped values are treated as doubles, derived integer types are not preserved
    if(type.isUntyped()) type = BasicType.DOUBLE;
    else if(type.instanceOf(BasicType.INTEGER)) type = BasicType.INTEGER;
    if(type.isNumber()) return type.seqType(st.occ.intersect(Occ.ZERO_OR_ONE));
    return null;
  }
}
