package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnCompare extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toTokenOrNull(arg(0), qc);
    final byte[] value2 = toTokenOrNull(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    if(value1 == null || value2 == null) return Empty.VALUE;
    final long diff = compare(value1, value2, collation);
    return Int.get(diff < 0 ? -1 : diff > 0 ? 1 : 0);
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
