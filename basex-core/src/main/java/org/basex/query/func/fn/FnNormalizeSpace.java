package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
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
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0);
    // normalize-space(normalize-space(E)) → normalize-space(E)
    if(Function.NORMALIZE_SPACE.is(value)) return value;
    // normalize-space(trim-space(E)) → normalize-space(E)
    if(Function.TRIM_SPACE.is(value) && value.arg(1) instanceof Value)
      return cc.function(Function.NORMALIZE_SPACE, info, value.arg(0));
    return this;
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
