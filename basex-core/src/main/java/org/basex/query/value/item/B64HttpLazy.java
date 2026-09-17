package org.basex.query.value.item;

import static org.basex.util.http.HTTPText.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.zip.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}) for HTTP response bodies.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class B64HttpLazy extends B64Lazy {
  /** Target URI. */
  private final URI uri;
  /** HTTP exchange for repeating the request (can be {@code null}). */
  private final Exchange exchange;
  /** Content encoding of the original response. */
  private final String encoding;
  /** Registry for temporary files (can be {@code null}). */
  private final TempFiles temp;
  /** Unread body of the original response (consumed by the first access). */
  private InputStream pending;
  /** Spilled body of a request that must not be repeated (can be {@code null}). */
  private IO body;

  /**
   * Constructor.
   * @param exchange HTTP exchange
   * @param pending unread body of the original response
   * @param encoding content encoding of the original response
   * @param temp registry for temporary files (can be {@code null})
   */
  public B64HttpLazy(final Exchange exchange, final InputStream pending, final String encoding,
      final TempFiles temp) {
    uri = exchange.uri();
    this.exchange = "GET".equals(exchange.method()) ? exchange : null;
    this.pending = pending;
    this.encoding = encoding;
    this.temp = temp;
  }

  @Override
  BufferInput open() throws IOException {
    if(body != null) return BufferInput.get(body);

    final InputStream is = pending;
    pending = null;
    if(exchange != null) {
      if(is != null) return decode(is, encoding);
      final HttpResponse<InputStream> response = exchange.send();
      return decode(response.body(), response.headers().firstValue(CONTENT_ENCODING).orElse(""));
    }
    // other requests: read the body once, spill it to disk if it is large
    if(is == null) throw new IOException("Response body is not available anymore.");
    final IO io;
    try(BufferInput bi = decode(is, encoding)) {
      io = SpillOutput.read(bi, temp);
    }
    if(io instanceof IOFile) body = io;
    else data = io.read();
    return BufferInput.get(io);
  }

  /**
   * Returns a buffered input stream for a response body.
   * @param is response body
   * @param enc content encoding
   * @return buffered input
   * @throws IOException I/O exception
   */
  private static BufferInput decode(final InputStream is, final String enc) throws IOException {
    return BufferInput.get(GZIP.equalsIgnoreCase(enc) ? new GZIPInputStream(is) : is);
  }

  @Override
  QueryException exception(final IOException ex, final InputInfo ii) {
    return Client.error(ex, ii);
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) super.toString(qs);
    else qs.function(Function._HTTP_SEND_REQUEST, uri);
  }
}
