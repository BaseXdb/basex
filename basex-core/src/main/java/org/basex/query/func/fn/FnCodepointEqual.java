package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
public final class FnCodepointEqual extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value1 = exprs[0].atomItem(qc, info), value2 = exprs[1].atomItem(qc, info);
    return value1.isEmpty() || value2.isEmpty() ? Empty.VALUE :
      Bln.get(eq(toToken(value1), toToken(value2)));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value1 = exprs[0], value2 = exprs[1];
    final SeqType st1 = value1.seqType(), st2 = value2.seqType();
    if(st1.zero()) return value1;
    if(st2.zero()) return value2;
    if(st1.oneOrMore() && !st1.mayBeArray() && st2.oneOrMore() && !st2.mayBeArray())
      exprType.assign(Occ.EXACTLY_ONE);
    return this;
  }
}
