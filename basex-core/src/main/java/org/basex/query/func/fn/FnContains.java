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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnContains extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.contains(string, sub) : coll.contains(string, sub, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    // contains($a, ''), contains($a, $a)
    if(exprs.length < 3 && expr1.seqType().type.isStringOrUntyped() && !expr1.has(Flag.NDT)) {
      if(expr2 == Empty.VALUE || expr2 == Str.EMPTY || expr1.equals(expr2)) return Bln.TRUE;
    }
    return this;
  }
}
