package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnStringLength extends ContextFn {
  @Override
  protected Itr item(final QueryContext qc) throws QueryException {
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
      if(v != null && !v.seqType().mayBeWrapped()) {
        return cc.function(STRING_LENGTH, info, value.args());
      }
    }
    if(STRING_JOIN.is(value)) {
      final Expr separator = value.arg(1);
      // string-length(string-join(E)) → sum(E ! string-length(), 0)
      if(separator == Empty.UNDEFINED || separator == Str.EMPTY) return lengths(value.arg(0), cc);
    } else if(value instanceof Concat) {
      // string-length(A || B) → sum((A, B) ! string-length(), 0)
      return lengths(List.get(cc, info, value.args()), cc);
    }
    return this;
  }

  /**
   * Rewrites the string length of concatenated values to a sum of string lengths.
   * @param values values to be concatenated
   * @param cc compilation context
   * @return sum expression or {@code null}
   * @throws QueryException query exception
   */
  private Expr lengths(final Expr values, final CompileContext cc) throws QueryException {
    if(values.seqType().mayBeWrapped()) return this;
    final Expr func = cc.get(values, true, () -> cc.function(STRING_LENGTH, info));
    return cc.function(SUM, info, SimpleMap.get(cc, info, values, func), Itr.ZERO);
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
