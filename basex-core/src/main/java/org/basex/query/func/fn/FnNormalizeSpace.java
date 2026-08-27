package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnNormalizeSpace extends ContextFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final Item item = context(qc).item(qc, info);
    return item.isEmpty() ? Str.EMPTY : Str.get(Token.normalize(item.string(info)));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    return !Token.ws(toZeroToken(context(qc), qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // normalize-space(normalize-space(E)) → normalize-space(E)
    return Function.NORMALIZE_SPACE.is(arg(0)) ? arg(0) : this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // $node[normalize-space(.)] → $node[descendant::text()[normalize-space(.)]]
      final Expr item = contextAccess() ? ContextValue.get(cc, info) : arg(0);
      expr = simplifyEbv(item, cc, () -> cc.function(Function.NORMALIZE_SPACE, info));
    }
    return cc.simplify(this, expr, mode);
  }
}
