package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnData extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomValue(qc, info);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ctxArg(0, qc).atomItem(qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final boolean context = contextAccess();
    final Expr expr = context ? cc.qc.focus.value : exprs[0];
    if(expr != null) {
      final SeqType st = expr.seqType();
      if(st.zero()) return expr;
      final AtomType type = st.type.atomic();
      if(type == st.type) {
        // data('x') -> 'x'
        // $string[data() = 'a'] -> $string[. = 'a']
        return context && cc.nestedFocus() ? new ContextValue(info).optimize(cc) : expr;
      }
      if(type != null) exprType.assign(type, st.occ, expr.size());
    }
    return this;
  }

  @Override
  public Expr simplify(final CompileContext cc, final Simplify simplify)
      throws QueryException {

    if(simplify == Simplify.ATOM) {
      // data(<a/>) = ''  ->  <a/> = ''
      if(!contextAccess()) return cc.simplify(this, exprs[0]);
      // A[B ! data() = '']  ->  A[B = '']
      if(cc.nestedFocus()) return new ContextValue(info).optimize(cc);
    }
    return super.simplify(cc, simplify);
  }
}
