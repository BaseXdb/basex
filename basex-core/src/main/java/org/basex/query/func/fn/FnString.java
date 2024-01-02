package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnString extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = context(qc).item(qc, info);

    if(value.isEmpty()) return Str.EMPTY;
    if(value.type == AtomType.STRING) return value;
    if(!(value instanceof FItem) || value instanceof XQJava) return Str.get(value.string(info));

    throw FISTRING_X.get(info, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // string(data(E))  ->  string(E)
    exprs = simplifyAll(Simplify.STRING, cc);

    final boolean context = contextAccess();
    final Expr item = context ? cc.qc.focus.value : arg(0);
    if(item != null && item.seqType().eq(SeqType.STRING_O)) {
      // string('x')  ->  'x'
      // $string[string() = 'a']  ->  $string[. = 'a']
      return context && cc.nestedFocus() ? ContextValue.get(cc, info) : item;
    }
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    final Expr item = contextAccess() ? ContextValue.get(cc, info) : arg(0);
    final SeqType st = item.seqType();
    if(mode == Simplify.STRING && st.type.isStringOrUntyped() && st.one()) {
      // $node[string() = 'x']  ->  $node[. = 'x']
      expr = item;
    } else if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // boolean(string($node))  ->  boolean($node/descendant::text())
      expr = simplifyEbv(item, cc, null);
    }
    return cc.simplify(this, expr, mode);
  }
}
