package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WsSet extends WsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final WebSocket client = client(qc);
    final String key = Token.string(toToken(exprs[1], qc));

    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = exprs[2].iter(qc);
    for(Item item; (item = iter.next()) != null;) {
      final Item it = item.materialize(qc, item.persistent());
      if(it == null) throw WS_SET_X.get(info, item);
      vb.add(it);
    }
    client.atts.put(key, vb.value());

    return null;
  }
}
