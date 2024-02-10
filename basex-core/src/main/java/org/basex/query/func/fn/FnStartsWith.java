package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnStartsWith extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] substring = toZeroToken(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    return Bln.get(collation == null ? Token.startsWith(value, substring) :
      collation.startsWith(value, substring, info));
  }
}
