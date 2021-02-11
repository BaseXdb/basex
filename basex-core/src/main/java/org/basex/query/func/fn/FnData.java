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
 * @author BaseX Team 2005-21, BSD License
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
        // data('x')  ->  'x'
        // $string[data() = 'a']  ->  $string[. = 'a']
        return context && cc.nestedFocus() ? ContextValue.get(cc, info) : expr;
      }
      if(type != null) exprType.assign(SeqType.get(type, st.occ), expr.size());
    }
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // data(xs:untypedAtomic(E))  ->  data(E)
    simplifyAll(Simplify.DATA, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = null;
    final Expr expr1 = contextAccess() ? ContextValue.get(cc, info) : exprs[0];
    if(mode == Simplify.DATA || mode == Simplify.STRING || mode == Simplify.NUMBER) {
      // data(<a/>) = ''  ->  <a/> = ''
      // A[B ! data() = '']  ->  A[B ! . = '']
      expr = expr1;
    } else if(mode == Simplify.EBV || mode == Simplify.PREDICATE) {
      // if(data($node))  ->  if($node/descendant::text())
      expr = simplifyEbv(expr1, cc);
    }
    return expr != null ? cc.simplify(this, expr) : super.simplifyFor(mode, cc);
  }

  @Override
  public boolean inlineable() {
    return contextAccess() || exprs[contextArg()] instanceof ContextValue;
  }
}
