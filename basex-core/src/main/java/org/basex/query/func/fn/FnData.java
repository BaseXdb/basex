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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnData extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return context(qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return context(qc).atomValue(qc, info);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return context(qc).atomItem(qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final boolean context = contextAccess();
    final Expr input = context ? cc.qc.focus.value : arg(0);

    if(input != null) {
      final SeqType st = input.seqType();
      if(st.zero()) return input;
      final AtomType type = st.type.atomic();
      if(type == st.type) {
        // data('x')  ->  'x'
        // $string[data() = 'a']  ->  $string[. = 'a']
        return context && cc.nestedFocus() ? ContextValue.get(cc, info) : input;
      }
      // ignore arrays: data((1 to 6) ! [ ., . ])
      if(type != null) {
        exprType.assign(SeqType.get(type, st.occ), st.mayBeArray() ? -1 : input.size());
      }
    }
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // data(xs:untypedAtomic(E))  ->  data(E)
    exprs = simplifyAll(Simplify.DATA, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    final Expr input = contextAccess() ? ContextValue.get(cc, info) : arg(0);
    if(mode.oneOf(Simplify.DATA, Simplify.NUMBER, Simplify.STRING)) {
      // data(<a/>) = ''  ->  <a/> = ''
      // A[B ! data() = '']  ->  A[B ! . = '']
      expr = input;
    } else if(mode == Simplify.COUNT) {
      // count(data($x))  ->  count($x)
      // do not simplify array input: count(data([ 1, 2, 3 ]))
      if(!input.seqType().mayBeArray()) expr = input;
    } else if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // if(data($node))  ->  if($node/descendant::text())
      expr = simplifyEbv(input, cc, null);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public boolean inlineable() {
    return contextAccess() || arg(contextIndex()) instanceof ContextValue;
  }
}
