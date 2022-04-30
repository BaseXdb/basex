package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class XQueryParseUri extends XQueryParse {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    return parse(qc, toIO(0, qc));
  }
}
