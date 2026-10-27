package org.basex.query.func.fn;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAvailableEnvironmentVariables extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(!trusted(qc)) return Empty.VALUE;
    checkPerm(qc, Perm.ADMIN);
    final TokenList tl = new TokenList();
    for(final Object key : System.getenv().keySet().stream().sorted().toList()) {
      tl.add(key.toString());
    }
    return StrSeq.get(tl);
  }
}
