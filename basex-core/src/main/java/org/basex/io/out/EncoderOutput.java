package org.basex.io.out;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

import org.basex.util.*;

/**
 * This class is a wrapper for outputting texts with specific encodings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class EncoderOutput extends PrintOutput {
  /** Encoding. */
  private final Charset encoding;
  /** Charset encoder. */
  private final CharsetEncoder encoder;
  /** Encoding buffer. */
  private final TokenBuilder encbuffer;

  /**
   * Constructor, given an output stream.
   * @param os output stream reference
   * @param encoding encoding
   */
  public EncoderOutput(final OutputStream os, final Charset encoding) {
    super(os);
    this.encoding = encoding;
    encoder = encoding.newEncoder();
    encbuffer = new TokenBuilder();
  }

  @Override
  public void print(final int ch) throws IOException {
    encbuffer.reset();
    encoder.reset();
    try {
      final ByteBuffer bb = encoder.encode(CharBuffer.wrap(encbuffer.add(ch).toString()));
      write(bb.array(), 0, bb.limit());
    } catch(final UnmappableCharacterException ex) {
      Util.debug(ex);
      throw SERENC_X_X.getIO(Integer.toHexString(ch), encoding);
    }
  }

  @Override
  public void print(final byte[] token) throws IOException {
    print(string(token));
  }

  @Override
  public void print(final String string) throws IOException {
    write(string.getBytes(encoding));
  }
}
