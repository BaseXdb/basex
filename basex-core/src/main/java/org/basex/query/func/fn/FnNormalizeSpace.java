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
  public Str value(final QueryContext qc) throws QueryException {
    final Item item = context(qc).atomItem(qc, info);
    return item.isEmpty() ? Str.EMPTY : Str.get(Token.normalize(item.string(info)));
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    return !Token.ws(toZeroToken(context(qc), qc));
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // normalize-space(<a>{ $x }</a>) → normalize-space(xs:string($x))
    exprs = simplifyAll(Simplify.STRING, cc);
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
