package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnMatches extends RegExFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, info, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final byte[] literal = literal(pattern, flags);
    if(literal != null) return contains(value, literal);
    return pattern(pattern, flags, qc).matcher(string(value)).find();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0), pattern = arg(1);

    final SeqType st = value.seqType();
    if((st.zero() || st.one() && st.type.isStringOrUntyped()) &&
        pattern instanceof final Str str && !defined(2)) {
      if(str == Str.EMPTY) return Bln.TRUE;
      // rewrite literal match to fn:contains (which, unlike fn:matches, respects collations)
      if(sc().collation == null && literal(str.string(), Token.EMPTY) != null) {
        return cc.function(CONTAINS, info, args());
      }
    }
    return this;
  }
}
