package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.api.client.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ClientConnect extends ClientFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String host = toString(exprs[0], qc);
    final String username = toString(exprs[2], qc);
    final String password = toString(exprs[3], qc);
    final int port = (int) toLong(exprs[1], qc);
    try {
      return sessions(qc).add(new ClientSession(host, port, username, password));
    } catch(final IOException ex) {
      throw CLIENT_CONNECT_X.get(info, ex);
    }
  }
}
