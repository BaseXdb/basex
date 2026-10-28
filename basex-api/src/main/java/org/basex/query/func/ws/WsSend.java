package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class WsSend extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item message = message(qc);
    final Expr ids = arg(1);
    final Value values = ids.atomValue(qc, info);

    final StringList list = new StringList(values.size());
    for(final Item item : values) list.add(toString(item, ids));
    WsPool.send(message, info, list.finish());
    return Empty.VALUE;
  }
}
