package org.basex.query.func.fn;

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
public final class FnContains extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] ss = toEmptyToken(exprs[0], qc), sb = toEmptyToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.contains(ss, sb) : coll.contains(ss, sb, info));
  }
}
