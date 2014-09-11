package org.basex.query.func.client;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ClientInfo extends ClientFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    return Str.get(session(qc, false).info().replaceAll("\r\n?", "\n").trim());
  }
}
