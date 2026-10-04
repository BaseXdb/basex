package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.api.client.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientConnect extends ClientFn {
  @Override
  public Uri value(final QueryContext qc) throws QueryException {
    final String host = toString(arg(0), qc);
    final String username = toString(arg(2), qc);
    final String password = toString(arg(3), qc);
    final long port = toLong(arg(1), qc);
    if(port < 0 || port > 0xFFFF) throw CLIENT_CONNECT_X.get(info, "Invalid port: " + port);
    try {
      return sessions(qc).add(new ClientSession(host, (int) port, username, password));
    } catch(final IOException ex) {
      throw CLIENT_CONNECT_X.get(info, ex);
    }
  }
}
