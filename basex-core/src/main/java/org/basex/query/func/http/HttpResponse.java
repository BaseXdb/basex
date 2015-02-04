package org.basex.query.func.http;

import static org.basex.query.func.http.HttpText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
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
  public ValueIter getResponse(final HttpURLConnection conn, final boolean body, final byte[] mtype)
      throws IOException, QueryException {

    // check content type
    final String type = conn.getContentType();
    InputStream is = conn.getErrorStream();
    final boolean error = is != null;
    try {
      if(!error) is = conn.getInputStream();
    } catch(final IOException ex) {
      Util.debug(ex);
    }

    // construct <http:response/>
    final ValueBuilder vb = new ValueBuilder();
    final FElem response = new FElem(Q_RESPONSE).declareNS();
    final String msg = conn.getResponseMessage();
    response.add(STATUS, token(conn.getResponseCode()));
    response.add(MESSAGE, msg == null ? "" : msg);
    for(final String header : conn.getHeaderFields().keySet()) {
      if(header != null) {
        final FElem hdr = new FElem(Q_HEADER);
        hdr.add(NAME, token(header));
        hdr.add(VALUE, conn.getHeaderField(header));
        response.add(hdr);
      }
    }
    vb.add(response);

    // construct <http:body/>
    if(is != null) {
      try {
        final HttpPayload hp = new HttpPayload(is, body, info, options);
        response.add(hp.parse(error, type, mtype == null ? null : string(mtype)));
        if(body) vb.add(hp.payloads());
      } finally {
        is.close();
      }
    }
    return vb;
  }
}
