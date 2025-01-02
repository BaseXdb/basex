package org.basex.io.out;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class is a wrapper for outputting texts with specific encodings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class EncoderOutput extends PrintOutput {
  /** Cache with encoded characters. */
  private final IntObjMap<byte[]> cache = new IntObjMap<>();
  /** Charset encoder. */
  private final CharsetEncoder encoder;
  /** Character set. */
  private final Charset charset;

  /**
   * Constructor, given an output stream.
   * @param os output stream reference
   * @param charset character set
   */
  public EncoderOutput(final OutputStream os, final Charset charset) {
    super(os);
    this.charset = charset;
    encoder = charset.newEncoder();
  }

  @Override
  public void print(final int cp, final Fallback fallback) throws IOException {
    try {
      byte[] bytes = cache.get(cp);
      if(bytes == null) {
        bytes = encoder.encode(CharBuffer.wrap(Character.toChars(cp))).array();
        cache.put(cp, bytes);
      }
      write(bytes);
    } catch(final CharacterCodingException ex) {
      if(fallback != null) {
        fallback.print(cp);
      } else {
        Util.debug(ex);
        throw SERENC_X_X.getIO(Integer.toHexString(cp), charset);
      }
    }
    lineLength = cp == '\n' ? 0 : lineLength + 1;
  }

  @Override
  public void print(final byte[] token) throws IOException {
    print(string(token));
  }

  @Override
  public void print(final String string) throws IOException {
    for(final int cp : string.codePoints().toArray()) print(cp);
  }
}
