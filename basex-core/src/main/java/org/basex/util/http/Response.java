package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.Version;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
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
  /** Input information (can be {@code null}). */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;
  /** Query resources (can be {@code null}). */
  private final QueryResources resources;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param options main options
   */
  public Response(final InputInfo info, final MainOptions options) {
    this(info, options, null);
  }

  /**
   * Constructor for lazy retrieval of response bodies.
   * @param info input info (can be {@code null})
   * @param options main options
   * @param resources query resources
   */
  public Response(final InputInfo info, final MainOptions options,
      final QueryResources resources) {
    this.info = info;
    this.options = options;
    this.resources = resources;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param response HTTP response
   * @param body also return body
   * @param mtype media type provided by the user (can be {@code null})
   * @return result sequence of http:response and content items
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Value getResponse(final HttpResponse<InputStream> response, final boolean body,
      final String mtype) throws IOException, QueryException {

    // construct <http:response/>
    final int status = response.statusCode();
    final FBuilder root = FElem.build(Q_HTTP_RESPONSE).ns();
    root.attr(Q_STATUS, status).attr(Q_MESSAGE, IOUrl.reason(status));
    final URI uri = response.uri();
    final String href = uri != null ? IOUrl.stripUserInfo(uri.toString()) : null;
    if(href != null) root.attr(Q_HREF, href);
    if(response.version() != null) {
      root.attr(Q_VERSION, response.version() == Version.HTTP_2 ? "HTTP/2" : "HTTP/1.1");
    }

    // add headers (names are case-insensitive, lower-case in HTTP/2), skip pseudo-headers
    for(final Entry<String, List<String>> entry : response.headers().map().entrySet()) {
      final String name = entry.getKey();
      if(name != null && !name.startsWith(":")) {
        final String lc = name.toLowerCase(Locale.ENGLISH);
        for(final String value : entry.getValue()) {
          root.node(FElem.build(Q_HTTP_HEADER).attr(Q_NAME, lc).attr(Q_VALUE, value));
        }
      }
    }

    // add payload elements and contents
    final HttpHeaders headers = response.headers();
    final MediaType type = mtype != null ? new MediaType(mtype) :
      headers.firstValue(CONTENT_TYPE).map(MediaType::new).orElse(MediaType.TEXT_PLAIN);
    final String encoding = headers.firstValue(CONTENT_ENCODING).orElse("");

    final TempFiles temp = resources != null ? resources.index(TempFiles.class) : null;
    final ItemList items = new ItemList().add((Item) null);
    if(body && resources != null && Payload.binary(type) &&
        !"0".equals(headers.firstValue(CONTENT_LENGTH).orElse(""))) {
      // binary result: skip retrieval of response body, return lazy item
      final InputStream is = response.body();
      resources.add(is);
      root.node(FElem.build(Q_HTTP_BODY).attr(Q_MEDIA_TYPE, type.type()).finish());
      items.add(new B64HttpLazy(href, is, encoding, temp));
    } else {
      try(InputStream is = response.body()) {
        final Payload payload = new Payload(is, body, info, options);
        final ResponseBody parsed = payload.parse(type, encoding, temp);
        root.node(element(parsed));
        if(body) items.add(parsed.values());
      }
    }

    return items.set(0, root.finish()).value();
  }

  /**
   * Returns the body element of a parsed response body.
   * @param body parsed body
   * @return body element
   */
  private static FNode element(final ResponseBody body) {
    final FBuilder elem;
    if(body.boundary != null) {
      elem = FElem.build(Q_HTTP_MULTIPART).attr(Q_BOUNDARY, body.boundary);
      for(final ResponseBody part : body.parts) {
        for(final Entry<String, String> header : part.headers) {
          elem.node(FElem.build(Q_HTTP_HEADER).attr(Q_NAME, header.getKey()).
            attr(Q_VALUE, header.getValue()));
        }
        elem.node(FElem.build(Q_HTTP_BODY).attr(Q_MEDIA_TYPE, part.type));
      }
    } else {
      elem = FElem.build(Q_HTTP_BODY);
    }
    return elem.attr(Q_MEDIA_TYPE, body.type.type()).finish();
  }

}
