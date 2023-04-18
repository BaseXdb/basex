package org.basex.query.func.web;

import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class WebForward extends WebFn {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String location = createUrl(qc);
    return new FBuilder(new FElem(HTTPText.Q_REST_FORWARD)).declareNS().add(location).finish();
  }
}
