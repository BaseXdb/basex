package org.basex.query.func.db;

import static org.basex.query.util.Err.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbEvent extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] name = toToken(exprs[0], qc);
    try {
      final ArrayOutput ao = qc.value(exprs[1]).serialize(SerializerOptions.get(false));
      // throw exception if event is unknown
      if(!qc.context.events.notify(qc.context, name, ao.finish()))
        throw BXDB_EVENT_X.get(info, name);
      return null;
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }
}
