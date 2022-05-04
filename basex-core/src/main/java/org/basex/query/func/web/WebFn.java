package org.basex.query.func.web;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
abstract class WebFn extends StandardFunc {
  /** Response options. */
  public static class ResponseOptions extends Options {
    /** Status. */
    public static final NumberOption STATUS = new NumberOption(HttpText.STATUS);
    /** Message. */
    public static final StringOption MESSAGE = new StringOption(HttpText.MESSAGE);
  }

  /**
   * Creates a URL from the function arguments.
   * @param qc query context
   * @return generated url
   * @throws QueryException query exception
   */
  final String createUrl(final QueryContext qc) throws QueryException {
    final byte[] path = toToken(exprs[0], qc);
    final XQMap map = exprs.length < 2 ? XQMap.empty() : toMap(exprs[1], qc);
    final byte[] anchor = exprs.length < 3 ? Token.EMPTY : toToken(exprs[2], qc);

    final TokenBuilder tb = new TokenBuilder().add(path);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(info);
      for(final Item item : map.get(key, info)) {
        tb.add(c++ == 0 ? '?' : '&').add(Token.encodeUri(name, false));
        tb.add('=').add(Token.encodeUri(item.string(info), false));
      }
    }
    if(anchor.length > 0) tb.add('#').add(Token.encodeUri(anchor, false));
    return tb.toString();
  }

  /**
   * Creates a REST response.
   * @param response status and message
   * @param headers response headers
   * @param output serialization parameters (can be {@code null})
   * @return response
   * @throws QueryException query exception
   */
  final FElem createResponse(final ResponseOptions response, final HashMap<String, String> headers,
      final HashMap<String, String> output) throws QueryException {

    // root element
    final FElem rrest = new FElem(HttpText.Q_REST_RESPONSE).declareNS();

    // HTTP response
    final FElem hresp = new FElem(HttpText.Q_HTTP_RESPONSE).declareNS();
    for(final Option<?> o : response) {
      if(response.contains(o)) hresp.add(o.name(), response.get(o).toString());
    }
    headers.forEach((name, value) -> {
      if(!value.isEmpty()) {
        final FElem hheader = new FElem(HttpText.Q_HTTP_HEADER);
        hresp.add(hheader.add(HttpText.NAME, name).add(HttpText.VALUE, value));
      }
    });
    rrest.add(hresp);

    // serialization parameters
    if(output != null) {
      final SerializerOptions sopts = SerializerMode.DEFAULT.get();
      for(final String entry : output.keySet())
        if(sopts.option(entry) == null) throw QueryError.INVALIDOPTION_X.get(info, entry);

      final FElem param = new FElem(FuncOptions.Q_SPARAM).declareNS();
      output.forEach((name, value) -> {
        if(!value.isEmpty()) {
          final FElem out = new FElem(new QNm(QueryText.OUTPUT_PREFIX, name, QueryText.OUTPUT_URI));
          param.add(out.add(HttpText.VALUE, value));
        }
      });
      rrest.add(param);
    }

    return rrest;
  }
}
