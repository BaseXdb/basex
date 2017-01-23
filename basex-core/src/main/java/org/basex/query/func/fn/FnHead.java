package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnHead extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr e = exprs[0];
    return e.seqType().zeroOrOne() ? e.item(qc, info) : e.iter(qc).next();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    seqType = SeqType.get(exprs[0].seqType().type, Occ.ZERO_ONE);
    return this;
  }
}
