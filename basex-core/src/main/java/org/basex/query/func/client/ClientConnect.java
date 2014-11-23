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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ClientConnect extends ClientFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);

    final String host = Token.string(toToken(exprs[0], qc));
    final String user = Token.string(toToken(exprs[2], qc));
    final String pass = Token.string(toToken(exprs[3], qc));
    final int port = (int) toLong(exprs[1], qc);
    try {
      return sessions(qc).add(new ClientSession(host, port, user, pass));
    } catch(final IOException ex) {
      throw BXCL_CONN_X.get(info, ex);
    }
  }
}
