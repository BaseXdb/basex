package org.basex.query.func.web;

import static org.basex.query.QueryText.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class WebResponseHeader extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] path = toToken(exprs[0], qc);
    final FElem hhead = new FElem(QNm.get(HTTP_PREFIX, "header", HTTP_URI)).
        add("name", "Cache-Control").add("value", "max-age=3600,public");
    final FElem hresp = new FElem(QNm.get(HTTP_PREFIX, "response", HTTP_URI)).
        add("status", "302").add(hhead);

    final FElem omedi = new FElem(QNm.get(OUTPUT_PREFIX, SerializerOptions.MEDIA_TYPE.name(),
        OUTPUT_URI)).add("value", MediaType.get(Token.string(path)).toString());
    final FElem ometh = new FElem(QNm.get(OUTPUT_PREFIX, SerializerOptions.METHOD.name(),
        OUTPUT_URI)).add("value", "raw");
    final FElem oseri = new FElem(QNm.get(OUTPUT_PREFIX, SERIALIZATION_PARAMETERS, OUTPUT_URI)).
        add(omedi).add(ometh);

    return new FElem(QNm.get(REST_PREFIX, "response", REST_URI)).add(hresp).add(oseri);
  }
}
