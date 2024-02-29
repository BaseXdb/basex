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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class WebFn extends StandardFunc {
  /** Response options. */
  public static class ResponseOptions extends Options {
    /** Status. */
    public static final NumberOption STATUS = new NumberOption("status");
    /** Message. */
    public static final StringOption MESSAGE = new StringOption("message");
  }

  /**
   * Creates a URL from the function arguments.
   * @param qc query context
   * @return generated url
   * @throws QueryException query exception
   */
  final String createUrl(final QueryContext qc) throws QueryException {
    final byte[] href = toToken(arg(0), qc);
    final Item params = arg(1).item(qc, info);
    final byte[] anchor = toZeroToken(arg(2), qc);

    final XQMap map = params.isEmpty() ? XQMap.empty() : toMap(params);
    final TokenBuilder tb = new TokenBuilder().add(href);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(info);
      for(final Item item : map.get(key)) {
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
  final FNode createResponse(final ResponseOptions response, final HashMap<String, String> headers,
      final HashMap<String, String> output) throws QueryException {

    // root element
    final FBuilder rrest = FElem.build(HTTPText.Q_REST_RESPONSE).declareNS();

    // HTTP response
    final FBuilder hresp = FElem.build(HTTPText.Q_HTTP_RESPONSE).declareNS();
    for(final Option<?> o : response) {
      if(response.contains(o)) hresp.add(new QNm(o.name()), response.get(o));
    }
    headers.forEach((name, value) -> {
      if(!value.isEmpty()) {
        hresp.add(FElem.build(HTTPText.Q_HTTP_HEADER).add(HTTPText.Q_NAME, name).
            add(HTTPText.Q_VALUE, value));
      }
    });
    rrest.add(hresp);

    // serialization parameters
    if(output != null) {
      final SerializerOptions sopts = SerializerMode.DEFAULT.get();
      for(final String entry : output.keySet())
        if(sopts.option(entry) == null) throw QueryError.INVALIDOPTION_X.get(info, entry);

      final FBuilder param = FElem.build(FuncOptions.Q_SERIALIZTION_PARAMETERS).declareNS();
      output.forEach((name, value) -> {
        if(!value.isEmpty()) {
          final QNm qnm = new QNm(QueryText.OUTPUT_PREFIX, name, QueryText.OUTPUT_URI);
          param.add(FElem.build(qnm).add(HTTPText.Q_VALUE, value));
        }
      });
      rrest.add(param);
    }
    return rrest.finish();
  }
}
