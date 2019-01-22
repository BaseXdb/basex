package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class XQueryInvoke extends XQueryEval {
  @Override
  protected ItemList eval(final QueryContext qc) throws QueryException {
    return invoke(qc, false);
  }

  /**
   * Invokes the specified query.
   * @param qc query context
   * @param updating updating query
   * @return resulting value
   * @throws QueryException query exception
   */
  final ItemList invoke(final QueryContext qc, final boolean updating) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(0, qc);
    try {
      return eval(qc, string(io.read()), io.url(), updating);
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
