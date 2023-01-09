package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnString extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ctxArg(0, qc).item(qc, info);

    if(item == Empty.VALUE) return Str.EMPTY;
    if(item.type == AtomType.STRING) return item;
    if(!(item instanceof FItem) || item instanceof XQJava) return Str.get(item.string(info));

    throw FISTRING_X.get(info, item);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // string(data(E))  ->  string(E)
    simplifyAll(Simplify.STRING, cc);

    final boolean context = contextAccess();
    final Expr item = context ? cc.qc.focus.value : exprs[0];
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
    final Expr item = contextAccess() ? ContextValue.get(cc, info) : exprs[0];
    final SeqType st = item.seqType();
    if(mode == Simplify.STRING && st.type.isStringOrUntyped() && st.one()) {
      // $node[string() = 'x']  ->  $node[. = 'x']
      expr = item;
    } else if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // boolean(string($node))  ->  boolean($node/descendant::text())
      expr = simplifyEbv(item, cc);
    }
    return cc.simplify(this, expr, mode);
  }
}
