package org.basex.query.func.request;

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
public final class RequestHeaderNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList list = new TokenList();
    for(final String name : state(qc).headerNames()) list.add(name);
    return StrSeq.get(list);
  }
}
