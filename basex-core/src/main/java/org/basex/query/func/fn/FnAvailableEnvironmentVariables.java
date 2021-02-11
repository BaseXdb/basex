package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnAvailableEnvironmentVariables extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    final TokenList tl = new TokenList();
    for(final Object key : System.getenv().keySet()) tl.add(key.toString());
    return StrSeq.get(tl);
  }
}
