package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnEndsWith extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    return Bln.get(coll == null ? Token.endsWith(value, sub) : coll.endsWith(value, sub, info));
  }
}
