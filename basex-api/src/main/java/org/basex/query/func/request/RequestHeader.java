package org.basex.query.func.request;

import org.basex.query.*;
import org.basex.query.expr.*;
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
    for(final String value : state(qc).headers(name)) list.add(value);
    if(list.isEmpty()) {
      final Expr dflt = arg(1);
      for(final Item item : dflt.atomValue(qc, info)) list.add(toToken(item, dflt));
    }
    return StrSeq.get(list);
  }
}
