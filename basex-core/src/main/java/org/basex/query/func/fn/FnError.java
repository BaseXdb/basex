package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnError extends StandardFunc {
  /** Error this function was created from (can be {@code null}). */
  private QueryException cause;

  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final QNm code = toQNmOrNull(arg(0), qc);
    final String description = toStringOrNull(arg(1), qc);
    final Value value = defined(2) ? arg(2).value(qc) : null;
    throw new QueryException(info, code != null ? code : FUNERR1.qname(),
      description != null ? description : FUNERR1.message()).value(value).cause(cause);
  }

  @Override
  public StandardFunc copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final FnError sf = (FnError) super.copy(cc, vm);
    sf.cause = cause;
    return sf;
  }

  @Override
  public boolean vacuous() {
    return true;
  }

  @Override
  protected Expr typeCheck(final TypeCheck tc, final CompileContext cc) {
    return this;
  }

  /**
   * Creates an instance of this function.
   * @param ex exception to be raised
   * @param expr expression that caused the error message
   * @return function
   */
  public static StandardFunc get(final QueryException ex, final Expr expr) {
    final Str description = Str.get(ex.getLocalizedMessage());
    final FnError sf = (FnError) ERROR.get(ex.info(), ex.qname(), description);
    sf.cause = ex;
    sf.exprType.assign(expr.seqType());
    return sf;
  }
}
