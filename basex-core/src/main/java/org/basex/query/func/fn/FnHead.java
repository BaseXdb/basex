package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnHead extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr e = exprs[0];
    return e.seqType().zeroOrOne() ? e.item(qc, info) : e.iter(qc).next();
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    seqType = SeqType.get(exprs[0].seqType().type, Occ.ZERO_ONE);
    return this;
  }
}
