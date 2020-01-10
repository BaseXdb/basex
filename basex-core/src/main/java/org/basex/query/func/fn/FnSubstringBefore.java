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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringBefore extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toEmptyToken(exprs[0], qc), sub = toEmptyToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    if(coll == null) {
      final int p = indexOf(string, sub);
      return p == -1 ? Str.ZERO : Str.get(substring(string, 0, p));
    }
    return Str.get(coll.before(string, sub, info));
  }
}
