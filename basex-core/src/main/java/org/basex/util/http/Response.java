package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.http.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Rositsa Shadura
 */
public final class Response {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param info input info
   * @param options database options
   */
  public Response(final InputInfo info, final MainOptions options) {
    this.info = info;
    this.options = options;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param response HTTP response
   * @param body also return body
   * @param mtype media type provided by the user (can be {@code null})
   * @return result sequence of http:response and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  public Value getResponse(final HttpResponse<InputStream> response, final boolean body,
      final String mtype) throws IOException, QueryException {

    // construct <http:response/>
    final int status = response.statusCode();
    final FElem root = new FElem(Q_HTTP_RESPONSE).declareNS();
    root.add(STATUS, Token.token(status));
    root.add(MESSAGE, IOUrl.reason(status));

    // add headers
    for(final Entry<String, List<String>> entry : response.headers().map().entrySet()) {
      final String name = entry.getKey();
      if(name != null) {
        for(final String value : entry.getValue()) {
          root.add(new FElem(Q_HTTP_HEADER).add(NAME, name).add(VALUE, value));
        }
      }
    }

    // add payload elements and contents
    final ItemList items = new ItemList().add(root);
    try(InputStream is = response.body()) {
      final HttpHeaders headers = response.headers();
      final MediaType type = mtype != null ? new MediaType(mtype) :
        headers.firstValue(CONTENT_TYPE).map(MediaType::new).orElse(MediaType.TEXT_PLAIN);
      final String encoding = headers.firstValue(CONTENT_ENCODING).orElse("");

      final Payload payload = new Payload(is, body, info, options);
      root.add(payload.parse(type, encoding));
      if(body) items.add(payload.value());
    }

    return items.value();
  }
}
