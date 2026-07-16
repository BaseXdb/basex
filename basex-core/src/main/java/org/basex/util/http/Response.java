package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.RequestAttribute.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class Response {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;
  /** Target URI (can be {@code null}). */
  private final URI uri;
  /** Request data (can be {@code null}). */
  private final Request request;
  /** Query context (can be {@code null}). */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param options main options
   */
  public Response(final InputInfo info, final MainOptions options) {
    this(info, options, null, null, null);
  }

  /**
   * Constructor for lazy retrieval of response bodies.
   * @param info input info (can be {@code null})
   * @param options main options
   * @param uri target URI
   * @param request request data
   * @param qc query context
   */
  public Response(final InputInfo info, final MainOptions options, final URI uri,
      final Request request, final QueryContext qc) {
    this.info = info;
    this.options = options;
    this.uri = uri;
    this.request = request;
    this.qc = qc;
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
    final FBuilder root = FElem.build(Q_HTTP_RESPONSE).ns();
    root.attr(Q_STATUS, status).attr(Q_MESSAGE, IOUrl.reason(status));

    // add headers
    for(final Entry<String, List<String>> entry : response.headers().map().entrySet()) {
      final String name = entry.getKey();
      if(name != null) {
        for(final String value : entry.getValue()) {
          root.node(FElem.build(Q_HTTP_HEADER).attr(Q_NAME, name).attr(Q_VALUE, value));
        }
      }
    }

    // add payload elements and contents
    final HttpHeaders headers = response.headers();
    final MediaType type = mtype != null ? new MediaType(mtype) :
      headers.firstValue(CONTENT_TYPE).map(MediaType::new).orElse(MediaType.TEXT_PLAIN);
    final String encoding = headers.firstValue(CONTENT_ENCODING).orElse("");

    final ItemList items = new ItemList().add((Item) null);
    if(body && request != null && "GET".equals(request.attribute(METHOD)) &&
        Payload.binary(type) && !"0".equals(headers.firstValue(CONTENT_LENGTH).orElse(""))) {
      // binary result: skip retrieval of response body, return lazy item
      final InputStream is = new StoppableInputStream(response.body());
      qc.resources.add(is);
      root.node(FElem.build(Q_HTTP_BODY).attr(Q_MEDIA_TYPE, type.type()).finish());
      items.add(new B64HttpLazy(uri, request, is, encoding));
    } else {
      try(InputStream is = new StoppableInputStream(response.body())) {
        final Payload payload = new Payload(is, body, info, options);
        root.node(payload.parse(type, encoding));
        if(body) items.add(payload.value());
      }
    }

    return items.set(0, root.finish()).value();
  }
}
