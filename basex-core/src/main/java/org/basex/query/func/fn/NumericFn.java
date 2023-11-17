package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.type.*;

/**
 * Numeric function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class NumericFn extends StandardFunc {
  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;
    final SeqType st = optType(arg(0), false);
    if(st != null) exprType.assign(st);
    return this;
  }

  /**
   * Returns a numeric type for the specified type.
   * @param expr expression
   * @param normalize use xs:integer for integer subtypes
   * @return sequence type or {@code null}
   */
  protected static final SeqType optType(final Expr expr, final boolean normalize) {
    final SeqType st = expr.seqType();
    Type type = st.type;
    if(type.isUntyped()) type = AtomType.DOUBLE;
    else if(normalize && type.instanceOf(AtomType.INTEGER)) type = AtomType.INTEGER;
    if(type.isNumber()) return type.seqType(st.occ.intersect(Occ.ZERO_OR_ONE));
    return null;
  }
}
