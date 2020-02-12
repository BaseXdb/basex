package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnString extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = ctxArg(0, qc).item(qc, info);
    if(item == Empty.VALUE) return Str.ZERO;
    if(item.type == AtomType.STR) return item;

    if(item instanceof FItem) throw FISTRING_X.get(info, item.type);
    return Str.get(item.string(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    simplifyAll(AtomType.ATM, cc);

    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[0];
    if(expr != null && expr.seqType().eq(SeqType.STR_O)) {
      // string('x') -> 'x'
      // $string[string() = 'a'] -> $string[. = 'a']
      return context && cc.nestedFocus() ? new ContextValue(info).optimize(cc) : expr;
    }
    return this;
  }

  @Override
  public Expr simplifyFor(final AtomType type, final CompileContext cc) throws QueryException {
    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[0];
    if(type == AtomType.ATM && expr != null) {
      final SeqType st = expr.seqType();
      if(st.one() && st.type.isStringOrUntyped()) {
        // string(<a>1</a>) = '1'  ->  <a>1</a> = '1'
        if(!context) return cc.simplify(this, exprs[0]);
        // $node[string() = 'x']  ->  $node[. = 'x']
        if(cc.nestedFocus()) return new ContextValue(info).optimize(cc);
      }
    }
    return super.simplifyFor(type, cc);
  }
}
