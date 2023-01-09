package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnContains extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.contains(value, sub) : coll.contains(value, sub, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = exprs[0], sub = exprs[1];
    // contains($a, ''), contains($a, $a)
    if(exprs.length < 3 && value.seqType().type.isStringOrUntyped() && !value.has(Flag.NDT)) {
      if(sub == Empty.VALUE || sub == Str.EMPTY || value.equals(sub)) return Bln.TRUE;
    }
    return this;
  }
}
