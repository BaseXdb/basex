package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.util.http.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}) for HTTP response bodies.
 * The first access consumes the body of the original exchange; subsequent
 * accesses send the request again.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class B64HttpLazy extends B64Lazy {
  /** Target URI. */
  private final URI uri;
  /** Request data. */
  private final Request request;
  /** Content encoding of the original response. */
  private final String encoding;
  /** Unread body of the original response (consumed by the first access). */
  private InputStream pending;

  /**
   * Constructor.
   * @param uri target URI
   * @param request request data
   * @param pending unread body of the original response
   * @param encoding content encoding of the original response
   */
  public B64HttpLazy(final URI uri, final Request request, final InputStream pending,
      final String encoding) {
    super(HC_ERROR_X);
    this.uri = uri;
    this.request = request;
    this.pending = pending;
    this.encoding = encoding;
  }

  @Override
  BufferInput open() throws IOException {
    InputStream is = pending;
    String enc = encoding;
    if(is != null) {
      pending = null;
    } else {
      final HttpResponse<InputStream> response = Client.send(uri, request);
      is = new StoppableInputStream(response.body());
      enc = response.headers().firstValue(CONTENT_ENCODING).orElse("");
    }
    return BufferInput.get(GZIP.equalsIgnoreCase(enc) ? new GZIPInputStream(is) : is);
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) super.toString(qs);
    else qs.function(Function._HTTP_SEND_REQUEST, uri);
  }
}
