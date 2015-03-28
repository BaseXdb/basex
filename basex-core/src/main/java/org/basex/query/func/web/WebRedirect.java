package org.basex.query.func.web;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class WebRedirect extends WebFn {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] url = createUrl(toToken(exprs[0], qc),
        exprs.length < 2 ? Map.EMPTY : toMap(exprs[1], qc));

    final FElem hhead = new FElem(QNm.get(HTTP_PREFIX, "header", HTTP_URI)).
        add("name", "location").add("value", url);
    final FElem hresp = new FElem(QNm.get(HTTP_PREFIX, "response", HTTP_URI)).
        add("status", "302").add(hhead);
    return new FElem(QNm.get(REST_PREFIX, "response", REST_URI)).add(hresp);
  }
}
