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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WebRedirect extends WebFn {
  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] url = createUrl(toToken(exprs[0], qc),
        exprs.length < 2 ? XQMap.EMPTY : toMap(exprs[1], qc));

    final FElem hhead = new FElem(new QNm(HTTP_PREFIX, "header", HTTP_URI));
    hhead.add("name", "location").add("value", url);
    final FElem hresp = new FElem(new QNm(HTTP_PREFIX, "response", HTTP_URI)).declareNS();
    hresp.add("status", "302").add(hhead);
    final FElem rresp = new FElem(new QNm(REST_PREFIX, "response", REST_URI)).declareNS();
    rresp.add(hresp);

    return rresp;
  }
}
