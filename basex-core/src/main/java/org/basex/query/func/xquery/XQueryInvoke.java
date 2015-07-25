package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XQueryInvoke extends XQueryEval {
  @Override
  protected ItemList eval(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(exprs[0], qc);
    try {
      return eval(qc, io.read(), io.path(), false);
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
