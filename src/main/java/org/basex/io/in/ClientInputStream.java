package org.basex.io.in;

import java.io.IOException;
import java.io.InputStream;

/**
 * This server-side class wraps an {@link InputStream} filled by a database
 * client. The incoming bytes are decoded: {@code 0x00} is treated as
 * end of stream. {@code 0xFF} is treated as encoding flag and skipped.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ClientInputStream extends InputStream {
  /** Input stream. */
  private final InputStream input;

  /**
   * Constructor.
   * @param in buffer input to be wrapped
   */
  public ClientInputStream(final InputStream in) {
    input = in;
  }

  @Override
  public int read() throws IOException {
    final int b = input.read();
    return b == 0 ? -1 : b == 0xFF ? input.read() : b;
  }
}
