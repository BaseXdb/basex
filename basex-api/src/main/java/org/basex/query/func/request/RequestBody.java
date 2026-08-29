package org.basex.query.func.request;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RequestBody extends ApiFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      final IO body = requestContext(qc).body();
      return body.length() == 0 ? Empty.VALUE : B64.get(body, REQUEST_BODY);
    } catch(final IOException ex) {
      throw REQUEST_BODY.get(info).cause(ex);
    }
  }
}
