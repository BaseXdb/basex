package org.basex.query.func.prof;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProfRuntime extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toToken(exprs[0], qc);

    final Runtime rt = Runtime.getRuntime();
    switch(Token.string(name)) {
      case "max":        return Int.get(rt.maxMemory());
      case "total":      return Int.get(rt.totalMemory());
      case "used":       return Int.get(rt.totalMemory() - rt.freeMemory());
      case "processors": return Int.get(rt.availableProcessors());
    }
    throw PROF_OPTION_X.get(info, name);
  }
}
