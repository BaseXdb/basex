package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringBefore extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    if(string.length == 0 || sub.length == 0) return Str.EMPTY;

    if(coll == null) {
      final int pos = indexOf(string, sub);
      return pos == -1 ? Str.EMPTY : Str.get(substring(string, 0, pos));
    }
    return Str.get(coll.before(string, sub, info));
  }
}
