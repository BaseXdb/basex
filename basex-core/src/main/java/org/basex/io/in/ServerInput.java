package org.basex.io.in;

import java.io.*;

/**
 * This server-side class wraps an {@link InputStream} reference by a database client.
 * The incoming bytes are decoded:
 * <ul>
 * <li> {@code 0x00} is treated as end of stream, and -1 is returned</li>
 * <li> {@code 0xFF} is treated as encoding flag and skipped</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServerInput extends InputStream {
  /** Input stream. */
  private final InputStream input;
  /** All bytes have been read. */
  private boolean more = true;

  /**
   * Constructor.
   * @param input buffer input to be wrapped
   */
  public ServerInput(final InputStream input) {
    this.input = input;
  }

  @Override
  public int read() throws IOException {
    if(more) {
      final int b = input.read();
      if(b != 0) return b == 0xFF ? input.read() : b;
      more = false;
    }
    return -1;
  }

  /**
   * Flushes the remaining client data.
   * @throws IOException I/O exception
   */
  public void flush() throws IOException {
    while(read() != -1);
  }
}
