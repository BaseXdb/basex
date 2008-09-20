package org.basex.io;

import java.io.IOException;
import java.net.URL;
import org.basex.BaseX;
import org.xml.sax.InputSource;

/**
 * BaseX input stream.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  public boolean exists() {
    try {
      // enough?...
      //new URL(path).openConnection();
      new URL(path).openStream();
      return true;
    } catch(IOException ex) {
      BaseX.debug(ex);
      return false;
    }
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
