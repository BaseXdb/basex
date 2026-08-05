package org.basex.query.func.client;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ClientClose extends ClientFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    try {
      session(qc, true).close();
      return Empty.VALUE;
    } catch(final IOException ex) {
      throw CLIENT_COMMAND_X.get(info, ex);
    }
  }
}
