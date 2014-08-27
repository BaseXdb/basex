package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnStartsWith extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] ss = toEmptyToken(exprs[0], qc), sb = toEmptyToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.startsWith(ss, sb) : coll.startsWith(ss, sb, info));
  }
}
