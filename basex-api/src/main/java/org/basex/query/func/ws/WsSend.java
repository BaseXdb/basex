package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsSend extends WsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item message = arg(0).item(qc, info);
    final Value ids = arg(1).atomValue(qc, info);
    if(message.isEmpty()) throw typeError(message, Types.ITEM_O, info);

    final StringList list = new StringList(ids.size());
    for(final Item item : ids) list.add(toString(item));
    WsPool.send(message, list.finish());
    return Empty.VALUE;
  }
}
