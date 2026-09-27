package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.FnPadString.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnTrimSpace extends StandardFunc {
  /** Options. */
  public static final class TrimOptions extends Options {
    /** Option. */
    public static final EnumOption<Side> SIDE = new EnumOption<>("side", Side.BOTH);
  }

  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Item item = arg(0).atomItem(qc, info);
    final byte[] value = item.isEmpty() ? EMPTY : item.string(info);
    final Side side = options(1, TrimOptions::new, qc).get(TrimOptions.SIDE);

    int s = 0, e = value.length;
    if(side != Side.END) {
      while(s < e && ws(value[s])) s++;
    }
    if(side != Side.START) {
      while(e > s && ws(value[e - 1])) e--;
    }
    return Str.get(substring(value, s, e));
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // trim-space(<a>{ $x }</a>) → trim-space(xs:string($x))
    arg(0, arg -> arg.simplifyFor(Simplify.STRING, cc));
    super.simplifyArgs(cc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0);
    // trim-space(()) → ''
    if(value == Empty.VALUE) return Str.EMPTY;
    // trim-space(normalize-space(E)) → normalize-space(E)
    if(Function.NORMALIZE_SPACE.is(value)) return value;
    // trim-space(trim-space(E, O), O) → trim-space(E, O)
    if(Function.TRIM_SPACE.is(value) && arg(1).equals(value.arg(1))) return value;
    optOptions(1, TrimOptions::new, cc);
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE) && arg(1) instanceof Value) {
      // $node[trim-space(.)] → $node[normalize-space(.)]
      expr = cc.function(Function.NORMALIZE_SPACE, info, arg(0));
    }
    return cc.simplify(this, expr, mode);
  }
}
