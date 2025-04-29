package org.basex.query.func.request;

import java.util.*;

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
public final class RequestAttributeNames extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final TokenList tl = new TokenList();
    for(final String name : Collections.list(request(qc).getAttributeNames())) {
      if(!name.startsWith("org.eclipse.jetty.")) tl.add(name);
    }
    return StrSeq.get(tl);
  }
}
