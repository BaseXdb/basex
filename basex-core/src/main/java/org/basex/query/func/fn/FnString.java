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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnString extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ctxArg(0, qc).item(qc, info);
    if(item == Empty.VALUE) return Str.EMPTY;
    if(item.type == AtomType.STRING) return item;

    if(item instanceof FItem) throw FISTRING_X.get(info, item.type);
    return Str.get(item.string(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // string(data(E))  ->  string(E)
    simplifyAll(Simplify.STRING, cc);

    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[0];
    if(expr != null && expr.seqType().eq(SeqType.STRING_O)) {
      // string('x')  ->  'x'
      // $string[string() = 'a']  ->  $string[. = 'a']
      return context && cc.nestedFocus() ? ContextValue.get(cc, info) : expr;
    }
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = null;
    final Expr expr1 = contextAccess() ? ContextValue.get(cc, info) : exprs[0];
    final SeqType st1 = expr1.seqType();
    if(mode == Simplify.STRING && st1.type.isStringOrUntyped() && st1.one()) {
      // $node[string() = 'x']  ->  $node[. = 'x']
      expr = expr1;
    } else if(mode == Simplify.EBV || mode == Simplify.PREDICATE) {
      // boolean(string($node))  ->  boolean($node/descendant::text())
      expr = simplifyEbv(expr1, cc);
    }
    return expr != null ? cc.simplify(this, expr) : super.simplifyFor(mode, cc);
  }
}
