package org.basex.util.http;

import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class HttpResponse {
  /** Input information. */
  private final InputInfo info;
  /** Database options. */
  private final MainOptions options;

  /**
   * Constructor.
   * @param info input info
   * @param options database options
   */
  public HttpResponse(final InputInfo info, final MainOptions options) {
    this.info = info;
    this.options = options;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param conn HTTP connection
   * @param body also return body
   * @param mtype media type provided by the user (can be {@code null})
   * @return result sequence of http:response and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @SuppressWarnings("resource")
  public Value getResponse(final HttpURLConnection conn, final boolean body, final String mtype)
      throws IOException, QueryException {

    // result
    final ItemList items = new ItemList();

    // construct <http:response/>
    final FElem response = new FElem(Q_RESPONSE).declareNS();
    items.add(response);

    final String msg = conn.getResponseMessage();
    response.add(STATUS, token(conn.getResponseCode()));
    response.add(MESSAGE, msg == null ? "" : msg);
    // add <http:header/> elements
    for(final Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
      final String name = entry.getKey();
      if(name != null) {
        for(final String value : entry.getValue()) {
          response.add(new FElem(Q_HEADER).add(NAME, name).add(VALUE, value));
        }
      }
    }

    // construct <http:body/>
    boolean error = false;
    InputStream is;
    try {
      is = conn.getInputStream();
    } catch(final IOException ex) {
      Util.debug(ex);
      is = conn.getErrorStream();
      error = true;
    }

    if(is != null) {
      final String ctype = conn.getContentType();
      // error: adopt original type as content type
      final MediaType type = error || mtype == null ? ctype == null ? MediaType.TEXT_PLAIN :
        new MediaType(ctype) : new MediaType(mtype);

      final HttpPayload hp = new HttpPayload(is, body, info, options);
      try {
        response.add(hp.parse(type, error, conn.getHeaderField(CONTENT_ENCODING)));
        if(body) items.add(hp.payloads());
      } finally {
        is.close();
      }
    }
    return items.value();
  }
}
