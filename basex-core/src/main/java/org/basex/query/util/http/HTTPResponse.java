package org.basex.query.util.http;

import static org.basex.query.util.http.HTTPText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * HTTP response handler. Reads HTTP response and constructs the
 * {@code <http:response/>} element.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPResponse {
  /** Input information. */
  private final InputInfo info;
  /** Database properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param ii input info
   * @param pr database properties
   */
  public HTTPResponse(final InputInfo ii, final Prop pr) {
    info = ii;
    prop = pr;
  }

  /**
   * Constructs http:response element and reads HTTP response content.
   * @param conn HTTP connection
   * @param status indicates if content is required
   * @param utype content type provided by the user to interpret the
   *          response content
   * @return result sequence of <http:response/> and content items
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  public ValueIter getResponse(final HttpURLConnection conn, final byte[] status,
      final String utype) throws IOException, QueryException {

    // check content type
    final String type = conn.getContentType();
    InputStream is = conn.getErrorStream();
    final boolean error = is != null;
    if(!error) is = conn.getInputStream();

    // construct <http:response/>
    final FElem response = new FElem(Q_RESPONSE).declareNS();
    response.add(STATUS, token(conn.getResponseCode()));
    response.add(MESSAGE, conn.getResponseMessage());

    for(final String header : conn.getHeaderFields().keySet()) {
      if(header != null) {
        final FElem hdr = new FElem(Q_HEADER);
        hdr.add(NAME, token(header));
        hdr.add(VALUE, conn.getHeaderField(header));
        response.add(hdr);
      }
    }

    // construct <http:body/>
    final boolean st = status != null && Bln.parse(status, info);
    final HTTPPayload hp = new HTTPPayload(is, st, info, prop);
    response.add(hp.parse(error, type, utype));

    // result
    final ValueBuilder vb = new ValueBuilder().add(response);
    if(!st) vb.add(hp.payloads());
    return vb;
  }
}
