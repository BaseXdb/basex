package org.basex.io;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.InputSource;

/**
 * URL reference, wrapped into an IO representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /**
   * Constructor.
   * @param u url
   */
  public IOUrl(final String u) {
    path = u;
  }

  @Override
  public void cache() throws IOException {
    cache(new URL(path).openStream());
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(path);
  }

  @Override
  public BufferInput buffer() throws IOException {
    return new BufferInput(new URL(path).openStream());
  }

  @Override
  public IO merge(final IO f) {
    return this;
  }
}
