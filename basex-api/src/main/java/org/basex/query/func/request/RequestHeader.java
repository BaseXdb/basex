package org.basex.query.func.request;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestHeader extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toString(arg(0), qc);

    final TokenList list = new TokenList(1);
    for(final String value : Collections.list(request(qc).getHeaders(name))) {
      list.add(value);
    }
    if(list.isEmpty()) {
      final Value dflt = arg(1).atomValue(qc, info);
      for(final Item item : dflt) list.add(toToken(item));
    }
    return StrSeq.get(list);
  }
}
