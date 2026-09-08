package org.basex.io.out;

import java.io.*;

import org.basex.io.*;

/**
 * This class uses a byte buffer to speed up output stream processing.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public final class BufferOutput extends OutputStream {
  /** Byte buffer. */
  private final byte[] buffer;
  /** Reference to the data output stream. */
  private final OutputStream out;
  /** Current buffer position. */
  private int pos;

  /**
   * Returns a buffered output stream.
   * @param os output stream
   * @return stream
   */
  public static BufferOutput get(final OutputStream os) {
    return os instanceof final BufferOutput bo ? bo : new BufferOutput(os);
  }

  /**
   * Constructor.
   * @param file target file
   * @throws IOException I/O exception
   */
  public BufferOutput(final IOFile file) throws IOException {
    this(file.outputStream());
  }

  /**
   * Constructor with a default buffer size.
   * @param out the stream to write to
   */
  BufferOutput(final OutputStream out) {
    this(out, IO.BLOCKSIZE);
  }

  /**
   * Constructor with a specific buffer size.
   * @param out the stream to write to
   * @param bufsize buffer size
   */
  BufferOutput(final OutputStream out, final int bufsize) {
    this.out = out;
    buffer = new byte[bufsize];
  }

  @Override
  public void write(final int b) throws IOException {
    if(pos == buffer.length) flush();
    buffer[pos++] = (byte) b;
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if(len >= buffer.length) {
      flush();
      out.write(b, off, len);
    } else {
      if(pos + len > buffer.length) flush();
      System.arraycopy(b, off, buffer, pos, len);
      pos += len;
    }
  }

  @Override
  public void flush() throws IOException {
    if(pos > 0) {
      out.write(buffer, 0, pos);
      pos = 0;
    }
  }

  @Override
  public void close() throws IOException {
    try {
      flush();
    } finally {
      out.close();
    }
  }
}
