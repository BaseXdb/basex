package org.basex.query.func.string;

import static org.basex.query.QueryError.*;
import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringTokenSetRatio extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value1 = toStr(arg(0), qc), value2 = toStr(arg(1), qc);

    final int[] cps1 = value1.codepoints(info), cps2 = value2.codepoints(info);
    if(cps1.length > MAX_LENGTH || cps2.length > MAX_LENGTH)
      throw STRING_BOUNDS_X.get(info, MAX_LENGTH);
    return Dbl.get(TokenRatio.set(cps1, cps2));
  }
}
