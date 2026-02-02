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
    final Expr expr = optFirst();
    if(expr != this) return expr;
    final SeqType st = optType(arg(0));
    if(st != null) exprType.assign(st);
    return this;
  }

  /**
   * Returns a numeric type for the specified type.
   * @param expr expression
   * @return sequence type or {@code null}
   */
  protected static SeqType optType(final Expr expr) {
    final SeqType st = expr.seqType();
    Type type = st.type;
    if(type.isUntyped()) type = BasicType.DOUBLE;
    if(type.isNumber()) return type.seqType(st.occ.intersect(Occ.ZERO_OR_ONE));
    return null;
  }
}
