package org.basex.io;

import java.io.ByteArrayInputStream;
import org.xml.sax.InputSource;

/**
 * BaseX content.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IOContent extends IO {
  /**
   * Constructor.
   * @param c contents
   */
  public IOContent(final byte[] c) {
    this(c, "tmp");
  }

  /**
   * Constructor.
   * @param c contents
   * @param p content path
   */
  public IOContent(final byte[] c, final String p) {
    super();
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
}
