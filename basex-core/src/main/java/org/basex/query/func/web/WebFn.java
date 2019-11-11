package org.basex.query.func.web;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * Returns a URL for the specified main URL and the query parameters.
   * @param url url
   * @param map query parameters
   * @param anchor anchor
   * @return generated url
   * @throws QueryException query exception
   */
  byte[] createUrl(final byte[] url, final XQMap map, final byte[] anchor)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder().add(url);
    int c = 0;
    for(final Item key : map.keys()) {
      final byte[] name = key.string(info);
      for(final Item value : map.get(key, info)) {
        tb.add(c++ == 0 ? '?' : '&').add(Token.encodeUri(name, false));
        tb.add('=').add(Token.encodeUri(value.string(info), false));
      }
    }
    if(anchor.length > 0) tb.add('#').add(Token.encodeUri(anchor, false));
    return tb.finish();
  }

  /**
   * Creates a REST response.
   * @param output serialization parameters
   * @param headers response headers
   * @param response status and message
   * @return response
   * @throws QueryException query exception
   */
  FElem createResponse(final HashMap<String, String> output, final HashMap<String, String> headers,
      final ResponseOptions response) throws QueryException {

    // check keys
    final SerializerOptions so = SerializerMode.DEFAULT.get();
    for(final String entry : output.keySet())
      if(so.option(entry) == null) throw INVALIDOPTION_X.get(info, entry);

    final FElem hresp = new FElem(new QNm(HTTP_PREFIX, "response", HTTP_URI)).declareNS();
    for(final Option<?> o : response) {
      if(response.contains(o)) hresp.add(o.name(), response.get(o).toString());
    }

    headers.forEach((name, value) -> {
      if(!value.isEmpty()) hresp.add(new FElem(new QNm(HTTP_PREFIX, "header", HTTP_URI)).
          add("name", name).add("value", value));
    });

    final FElem oseri = new FElem(FuncOptions.Q_SPARAM).declareNS();
    output.forEach((name, value) -> {
      if(!value.isEmpty()) oseri.add(new FElem(new QNm(OUTPUT_PREFIX, name, OUTPUT_URI)).
          add("value", value));
    });

    // REST response
    final FElem rrest = new FElem(new QNm(REST_PREFIX, "response", REST_URI)).declareNS();

    return rrest.add(hresp).add(oseri);
  }

}
