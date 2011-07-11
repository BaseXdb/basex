package org.basex.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.basex.util.ByteList;
import org.xml.sax.InputSource;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class IOUrl extends IO {
  /**
   * Constructor.
   * @param u url
   */
  IOUrl(final String u) {
    super(u);
  }

  @Override
  public void cache() throws IOException {
    final ByteList bl = new ByteList();
    final InputStream bis = new BufferedInputStream(new URL(path).openStream());
    for(int b; (b = bis.read()) != -1;) bl.add(b);
    bis.close();
    cont = bl.toArray();
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
  public IO merge(final String f) {
    return this;
  }
}
