package org.basex.io;

import java.io.ByteArrayInputStream;
import org.basex.io.in.ArrayInput;
import org.basex.util.Token;
import org.xml.sax.InputSource;

/**
 * {@link IO} reference, representing a byte array.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IOContent extends IO {
  /** Content. */
  private final byte[] cont;

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
  public byte[] read() {
    return cont;
  }

  @Override
  public long length() {
    return cont.length;
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(new ByteArrayInputStream(cont));
  }

  @Override
  public ArrayInput inputStream() {
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
