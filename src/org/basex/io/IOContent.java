package org.basex.io;

import java.io.ByteArrayInputStream;

import org.basex.util.Token;
import org.xml.sax.InputSource;

/**
 * Byte contents, wrapped into an IO representation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
    cont = c;
    path = p;
  }

  @Override
  public void cache() { }

  @Override
  public InputSource inputSource() {
    return new InputSource(new ByteArrayInputStream(cont));
  }

  @Override
  public BufferInput buffer() {
    return new CachedInput(cont);
  }

  @Override
  public IO merge(final IO f) {
    return f;
  }

  @Override
  public String toString() {
    return Token.string(cont);
  }
}
