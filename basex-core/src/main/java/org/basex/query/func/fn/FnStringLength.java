package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = context(qc).item(qc, info);
    if(value.isEmpty()) return Itr.ZERO;
    // optimization to return pre-computed string length
    if(value instanceof final AStr str) return Itr.get(str.length(info));
    return Itr.get(Token.length(value.string(info)));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0);
    if(STRING.is(value)) {
      final Expr v = ((FnString) value).contextAccess() ? cc.qc.focus.value : value.arg(0);
      // string-length(string(E)) → string-length(E)
      if(v != null && !v.seqType().mayBeFunction()) {
        return cc.function(STRING_LENGTH, info, value.args());
      }
    }
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.EBV) {
      // if(string-length(E)) → if(string(E))
      expr = cc.function(STRING, info, exprs);
    }
    return cc.simplify(this, expr, mode);
  }
}
