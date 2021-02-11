package org.basex.io.out;

import java.io.*;

/**
 * This server-side class wraps an {@link InputStream} referenced by a database client.
 * The incoming bytes are encoded:
 * <ul>
 * <li>{@code 0x00} and {@code 0xFF} are prefixed with {@code 0xFF}</li>
 * <li>{@code 0x00} is sent to indicate the end of a stream</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServerOutput extends OutputStream {
  /** Output stream. */
  private final OutputStream os;

  /**
   * Constructor.
   * @param os output stream to be wrapped
   */
  public ServerOutput(final OutputStream os) {
    this.os = os;
  }

  @Override
  public void write(final int b) throws IOException {
    if(b == 0x00 || (b & 0xFF) == 0xFF) os.write(0xFF);
    os.write(b);
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public void close() throws IOException {
    os.close();
  }
}
