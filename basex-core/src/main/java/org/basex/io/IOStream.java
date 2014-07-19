package org.basex.io;

import java.io.*;

import javax.xml.transform.stream.*;

import org.basex.io.in.*;
import org.xml.sax.*;

/**
 * {@link IO} stream.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IOStream extends IO {
  /** Buffered input stream. */
  private final BufferInput input;

  /**
   * Constructor.
   * @param is input stream
   */
  public IOStream(final InputStream is) {
    this(is, "");
  }

  /**
   * Constructor.
   * @param is input stream
   * @param path path
   */
  public IOStream(final InputStream is, final String path) {
    super(path);
    input = is instanceof BufferInput ? (BufferInput) is : new BufferInput(is);
  }

  @Override
  public byte[] read() throws IOException {
    return input.content();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(input);
  }

  @Override
  public StreamSource streamSource() {
    return new StreamSource(input);
  }

  @Override
  public InputStream inputStream() {
    return input;
  }
}
