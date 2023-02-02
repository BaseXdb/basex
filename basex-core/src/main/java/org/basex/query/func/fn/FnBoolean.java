package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // boolean(true()))  ->  true()
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.eq(SeqType.BOOLEAN_O)) return input;

    // boolean($node/text())  ->  exists($node/text())
    if(st.type instanceof NodeType) return cc.function(Function.EXISTS, info, input);

    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // boolean(exists(<a/>))  ->  boolean(<a/>)
    exprs[0] = exprs[0].simplifyFor(Simplify.EBV, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // if(boolean(number))  ->  if(number)
    // E[boolean(nodes)]  ->  E[nodes]
    final Expr input = exprs[0];
    return cc.simplify(this, mode == Simplify.EBV || mode == Simplify.PREDICATE &&
        !input.seqType().mayBeNumber() ? input : this, mode);
  }
}
