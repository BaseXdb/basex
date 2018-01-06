package org.basex.query.func.strings;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class StringsSoundex extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int[] cps = new TokenParser(toToken(exprs[0], qc)).toArray();
    return Str.get(new TokenBuilder(Soundex.encode(cps)).finish());
  }
}
