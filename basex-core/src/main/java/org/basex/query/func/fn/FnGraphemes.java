package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnGraphemes extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(!GraphemesOptions.AVAILABLE) throw QueryError.BASEX_ICU.get(info);

    final String value = toStringOrNull(arg(0), qc);
    final GraphemesOptions options = toOptions(arg(1), new GraphemesOptions(), true, qc);
    return value != null ? StrSeq.get(options.split(value)) : Empty.VALUE;
  }
}
