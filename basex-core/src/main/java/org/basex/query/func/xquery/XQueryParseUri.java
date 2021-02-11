package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQueryParseUri extends XQueryParse {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(0, qc);
    try {
      return parse(qc, io.read(), io.path());
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
