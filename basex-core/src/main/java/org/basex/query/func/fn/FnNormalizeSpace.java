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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnNormalizeSpace extends ContextFn {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Str.get(Token.normalize(toZeroToken(context(qc), qc)));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // $node[normalize-space(.)]  ->  $node[descendant::text()[normalize-space(.)]]
      final Expr item = contextAccess() ? ContextValue.get(cc, info) : arg(0);
      expr = simplifyEbv(item, cc, () -> cc.function(Function.NORMALIZE_SPACE, info));
    }
    return cc.simplify(this, expr, mode);
  }
}
