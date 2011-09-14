package org.basex.io.out;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This server-side class wraps an {@link InputStream} filled by a database
 * client. The incoming bytes are encoded:
 * {@code 0x00} and {@code 0xFF} are prefixed with {@code 0xFF}.
 * {@code 0x00} is sent to indicate the end of a stream.
 *
 * @author BaseX Team 2005-11, BSD License
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
    os.write(b);
  }

  /**
   * Passes on all bytes from the specified input stream.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public void write(final InputStream is) throws IOException {
    for(int b; (b = is.read()) != -1;) {
      if(b == 0x00 || b == 0xFF) os.write(0xFF);
      write(b);
    }
    os.write(0);
  }

  @Override
  public final void flush() throws IOException {
    os.flush();
  }

  @Override
  public final void close() throws IOException {
    os.close();
  }
}
