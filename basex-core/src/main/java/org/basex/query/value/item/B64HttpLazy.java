package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
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
  /** URI of the response, without credentials (can be {@code null}). */
  private final String href;
  /** Content encoding of the original response. */
  private final String encoding;
  /** Registry for temporary files (can be {@code null}). */
  private final TempFiles temp;
  /** Unread body of the original response (consumed by the first access). */
  private InputStream pending;

  /**
   * Constructor.
   * @param href URI of the response, without credentials (can be {@code null})
   * @param pending unread body of the original response
   * @param encoding content encoding of the original response
   * @param temp registry for temporary files (can be {@code null})
   */
  public B64HttpLazy(final String href, final InputStream pending, final String encoding,
      final TempFiles temp) {
    this.href = href;
    this.pending = pending;
    this.encoding = encoding;
    this.temp = temp;
  }

  @Override
  IO source() throws IOException {
    // read the body once, spill it to disk if it is large
    final InputStream is = pending;
    pending = null;
    if(is == null) throw new IOException("Response body is not available anymore.");
    try(InputStream in = Payload.decode(is, encoding)) {
      return SpillOutput.read(in, temp);
    }
  }

  @Override
  QueryException exception(final IOException ex, final InputInfo ii) {
    return Client.error(ex, ii);
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) super.toString(qs);
    else qs.function(Function._HTTP_SEND_REQUEST, href);
  }
}
