package org.basex.util.http;

import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team 2005-15, BSD License
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
   * @return result sequence of <http:response/> and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  @SuppressWarnings("resource")
  public ItemList getResponse(final HttpURLConnection conn, final boolean body, final String mtype)
      throws IOException, QueryException {

    // check content type
    boolean error = false;
    InputStream is = null;
    try {
      is = conn.getInputStream();
    } catch(final IOException ex) {
      Util.debug(ex);
      is = conn.getErrorStream();
      error = true;
    }

    // result
    final ItemList res = new ItemList();

    // construct <http:response/>
    final FElem response = new FElem(Q_RESPONSE).declareNS();
    res.add(response);

    final String msg = conn.getResponseMessage();
    response.add(STATUS, token(conn.getResponseCode()));
    response.add(MESSAGE, msg == null ? "" : msg);
    // add <http:header/> elements
    for(final String header : conn.getHeaderFields().keySet()) {
      if(header != null) {
        final FElem hdr = new FElem(Q_HEADER);
        hdr.add(NAME, header);
        hdr.add(VALUE, conn.getHeaderField(header));
        response.add(hdr);
      }
    }
    // construct <http:body/>
    if(is != null) {
      try {
        final HttpPayload hp = new HttpPayload(is, body, info, options);
        final String ctype = conn.getContentType();
        // error: adopt original type as content type
        final MediaType type = error || mtype == null ? ctype == null ? MediaType.TEXT_PLAIN :
          new MediaType(ctype) : new MediaType(mtype);
        response.add(hp.parse(type));
        if(body) res.add(hp.payloads());
      } finally {
        is.close();
      }
    }
    return res;
  }
}
