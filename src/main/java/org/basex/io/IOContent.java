package org.basex.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.basex.util.Token;
import org.xml.sax.InputSource;

/**
 * Byte contents, wrapped into an IO representation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IOContent extends IO {
  /**
   * Constructor.
   * @param c contents
   */
  public IOContent(final byte[] c) {
    this(c, "");
  }

  /**
   * Constructor.
   * @param c contents
   * @param p content path
   */
  public IOContent(final byte[] c, final String p) {
    super(p);
    cont = c;
  }

  @Override
  public void cache() { }

  @Override
  public InputSource inputSource() {
    return new InputSource(inputStream());
  }

  @Override
  public InputStream inputStream() {
    return new ByteArrayInputStream(cont);
  }

  @Override
  public BufferInput buffer() {
    return new ArrayInput(cont);
  }

  @Override
  public IO merge(final String f) {
    return IO.get(f);
  }

  @Override
  public String toString() {
    return Token.string(cont);
  }
}
