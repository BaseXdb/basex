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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCodepointEqual extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Boolean test = test(qc);
    return test != null ? Bln.get(test) : Empty.VALUE;
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Boolean test = test(qc);
    return test != null && test;
  }

  /**
   * Performs the test.
   * @param qc query context
   * @return result of check, or {@code null} if one input is an empty sequence
   * @throws QueryException query exception
   */
  private Boolean test(final QueryContext qc) throws QueryException {
    final Item value1 = arg(0).atomItem(qc, info);
    if(value1 == Empty.VALUE) return null;
    final Item value2 = arg(1).atomItem(qc, info);
    if(value2 == Empty.VALUE) return null;
    return eq(toToken(value1), toToken(value2));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value1 = arg(0), value2 = arg(1);
    final SeqType st1 = value1.seqType(), st2 = value2.seqType();
    if(st1.zero()) return value1;
    if(st2.zero()) return value2;
    if(st1.oneOrMore() && !st1.mayBeArray() && st2.oneOrMore() && !st2.mayBeArray())
      exprType.assign(Occ.EXACTLY_ONE);
    return this;
  }
}
