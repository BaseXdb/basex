package org.basex.io.out;

import java.io.*;

/**
 * This server-side class wraps an {@link InputStream} filled by a database
 * client. The incoming bytes are encoded:
 * <ul>
 * <li>{@code 0x00} and {@code 0xFF} are prefixed with {@code 0xFF}</li>
 * <li>{@code 0x00} is sent to indicate the end of a stream</li>
 * </ul>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class EncodingOutput extends OutputStream {
  /** Output stream. */
  private final OutputStream os;

  /**
   * Constructor.
   * @param out output stream to be wrapped
   */
  public EncodingOutput(final OutputStream out) {
    os = out;
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
