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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringBefore extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] ss = toEmptyToken(exprs[0], qc), sb = toEmptyToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    if(coll == null) {
      final int p = indexOf(ss, sb);
      return p == -1 ? Str.ZERO : Str.get(substring(ss, 0, p));
    }
    return Str.get(coll.before(ss, sb, info));
  }
}
