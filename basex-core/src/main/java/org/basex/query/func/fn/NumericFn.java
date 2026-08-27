package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
    // ceiling(floor(E)) → floor(E), round(round(E)) → round(E)
    final Expr input = arg(0);
    if(integral() && input instanceof final NumericFn fn && fn.integral()) return input;

    final Expr expr = optFirst();
    if(expr != this) return expr;
    final SeqType st = optType(input);
    if(st != null) exprType.assign(st);
    return this;
  }

  /**
   * Indicates if the function result is an integral number.
   * @return result of check
   */
  boolean integral() {
    return false;
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
