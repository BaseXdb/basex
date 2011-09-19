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
public final class DecodingInput extends InputStream {
  /** Input stream. */
  private final InputStream input;
  /** All bytes have been read. */
  private boolean more = true;

  /**
   * Constructor.
   * @param in buffer input to be wrapped
   */
  public DecodingInput(final InputStream in) {
    input = in;
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
