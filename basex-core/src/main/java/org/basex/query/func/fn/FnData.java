package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnData extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return contextArg(qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return atomValue(contextArg(qc), qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = contextArg(cc);

    if(input != null) {
      final SeqType st = input.seqType();
      // data(()) → ()
      if(st.zero()) return input instanceof Dummy ? Empty.VALUE : input;
      final BasicType type = st.type.atomic();
      if(type == st.type) {
        // data('x') → 'x'
        // $string[data() = 'a'] → $string[. = 'a']
        return contextExpr(input, cc);
      }
      // skip unknown atomic types: data([ <a/>, 1 ])
      if(type != null) exprType.assign(type.seqType(st.atomOcc()), input.atomSize());
    }
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // data(xs:untypedAtomic(E)) → data(E)
    exprs = simplifyAll(Simplify.DATA, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    final Expr input = usesContext() ? ContextValue.get(cc, info) : arg(0);
    // data(<a/>) = '' → <a/> = '', A[B ! data() = ''] → A[B ! . = '']
    // count(data($node)) → count($node), string(data($node)) → string($node)
    // excluded: string(data([ 1 ])), count(data([ 1, 2 ])), exists(data({ 'a': () }/a))
    final SeqType st = input.seqType();
    if(mode.atomizing() || mode == Simplify.STRING_VALUE && !st.mayBeWrapped() ||
        mode.oneOf(Simplify.COUNT, Simplify.EXISTENCE) && st.atomizesToOne()) {
      expr = input;
    } else if(mode.conditional()) {
      // if(data($node)) → if($node/descendant::text())
      expr = simplifyEbv(input, cc, null);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public boolean inlineable() {
    return usesContext() || arg(contextIndex()) instanceof ContextValue;
  }
}
