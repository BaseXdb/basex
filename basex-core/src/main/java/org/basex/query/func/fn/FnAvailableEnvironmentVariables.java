package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnAvailableEnvironmentVariables extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder();
    for(final Object k : System.getenv().keySet()) vb.add(Str.get(k.toString()));
    return vb.value();
  }
}
