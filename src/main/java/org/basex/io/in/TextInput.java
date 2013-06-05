package org.basex.io.in;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.util.*;

/**
 * This class provides a convenient access to text input.
 * The input encoding will be guessed by analyzing the first bytes.
 * UTF-8 will be used as fallback.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class TextInput extends BufferInput {
  /** Decoder. */
  private TextDecoder decoder;
  /** Indicates if input is checked for valid XML 1.0. */
  private boolean valid;

  /**
   * Constructor.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public TextInput(final InputStream is) throws IOException {
    super(is);
    guess();
  }

  /**
   * Constructor.
   * @param io input
   * @throws IOException I/O exception
   */
  public TextInput(final IO io) throws IOException {
    super(io);
    guess();
  }

  /**
   * Tries to guess the character encoding, based on the first bytes.
   * @throws IOException I/O exception
   */
  private void guess() throws IOException {
    try {
      final int a = next();
      final int b = next();
      final int c = next();
      final int d = next();
      String e = null;
      int skip = 0;
      if(a == 0xFF && b == 0xFE) { // BOM: FF FE
        e = UTF16LE;
        skip = 2;
      } else if(a == 0xFE && b == 0xFF) { // BOM: FE FF
        e = UTF16BE;
        skip = 2;
      } else if(a == 0xEF && b == 0xBB && c == 0xBF) { // BOM: EF BB BF
        skip = 3;
      } else if(a == '<' && b == 0 && c == '?' && d == 0) {
        e = UTF16LE;
      } else if(a == 0 && b == '<' && c == 0 && d == '?') {
        e = UTF16BE;
      }
      reset();
      for(int s = 0; s < skip; s++) next();
      decoder = TextDecoder.get(normEncoding(e));
    } catch(final IOException ex) {
      close();
      throw ex;
    }
  }

  /**
   * Returns the encoding.
   * @return encoding
   */
  public final String encoding() {
    return decoder.encoding;
  }

  /**
   * Checks input for valid XML 1.0 and throws exception if invalid characters are found.
   * @param v flag to be set
   * @return self reference
   */
  public final TextInput validate(final boolean v) {
    valid = v;
    decoder.valid = v;
    return this;
  }

  /**
   * Sets a new encoding if none has been set yet, or if specified encoding is not UTF-8.
   * @param enc encoding
   * @return self reference
   * @throws IOException I/O Exception
   */
  public TextInput encoding(final String enc) throws IOException {
    if(enc != null) {
      decoder = TextDecoder.get(normEncoding(enc, decoder.encoding));
      decoder.valid = valid;
    }
    return this;
  }

  @Override
  public int read() throws IOException {
    final int ch = decoder.read(this);
    if(ch != -1 && valid && !XMLToken.valid(ch)) throw new EncodingException(ch);
    return ch;
  }

  @Override
  public final byte[] content() throws IOException {
    return cache().finish();
  }

  /**
   * Retrieves the whole text and closes the stream.
   * @return token builder instance
   * @throws IOException I/O exception
   */
  public final TokenBuilder cache() throws IOException {
    final TokenBuilder tb = new TokenBuilder(Math.max(Array.CAPACITY, (int) length));
    try {
      for(int ch; (ch = read()) != -1;) tb.add(ch);
    } finally {
      close();
    }
    return tb;
  }
}
