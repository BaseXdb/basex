package org.basex.io.in;

import static org.basex.util.Strings.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides buffered access to textual input.
 * The input encoding will initially be guessed by analyzing the first bytes;
 * it can also be explicitly set by calling {@link #encoding(String)}.
 * UTF-8 will be used as default encoding.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class TextInput extends BufferInput {
  /** Decoder. */
  private TextDecoder decoder;
  /** Indicates if the input is to be checked for valid XML 1.0.5 characters. */
  private boolean validate;

  /**
   * Constructor.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public TextInput(final InputStream is) throws IOException {
    this(is, null);
  }

  /**
   * Constructor.
   * @param is input stream
   * @param encoding encoding (ignored if {@code null})
   * @throws IOException I/O exception
   */
  public TextInput(final InputStream is, final String encoding) throws IOException {
    this(is, encoding, true);
  }

  /**
   * Constructor.
   * @param is input stream
   * @param encoding encoding (ignored if {@code null})
   * @param guess guess the encoding
   * @throws IOException I/O exception
   */
  public TextInput(final InputStream is, final String encoding, final boolean guess)
      throws IOException {
    super(is);
    if(guess) {
      guess(encoding);
    } else {
      decoder = TextDecoder.get(normEncoding(encoding, true));
    }
  }

  /**
   * Constructor.
   * @param io input
   * @throws IOException I/O exception
   */
  public TextInput(final IO io) throws IOException {
    this(io, null);
  }

  /**
   * Constructor.
   * @param io input
   * @param encoding encoding (ignored if {@code null})
   * @throws IOException I/O exception
   */
  public TextInput(final IO io, final String encoding) throws IOException {
    super(io);
    guess(encoding);
  }

  /**
   * Reads the first bytes of the input stream to guess the text encoding.
   * @param encoding encoding (ignored if {@code null})
   * @throws IOException I/O exception
   */
  private void guess(final String encoding) throws IOException {
    try {
      final int a = readByte(), b = readByte(), c = readByte(), d = readByte();
      String enc = normEncoding(encoding, false);
      int skip = 0;
      if(Strings.eq(enc, UTF8, null) && a == 0xEF && b == 0xBB && c == 0xBF) {
        enc = UTF8;
        skip = 3;
      } else if(Strings.eq(enc, UTF16, UTF16LE, null) && a == 0xFF && b == 0xFE) {
        enc = UTF16LE;
        skip = 2;
      } else if(Strings.eq(enc, UTF16, UTF16BE, null) && a == 0xFE && b == 0xFF) {
        enc = UTF16BE;
        skip = 2;
      } else if(Strings.eq(enc, UTF16, null) && a == '<' && b == 0 && c == '?' && d == 0) {
        enc = UTF16LE;
      } else if(Strings.eq(enc, UTF16, null) && a == 0 && b == '<' && c == 0 && d == '?') {
        enc = UTF16BE;
      } else if(Strings.eq(enc, UTF16)) {
        enc = UTF16BE;
      } else if(enc == null) {
        enc = UTF8;
      }
      reset();
      for(int s = 0; s < skip; s++) readByte();
      decoder = TextDecoder.get(enc);
    } catch(final IOException ex) {
      close();
      throw ex;
    }
  }

  /**
   * Checks the input for valid XML characters and throws an exception if invalid
   * characters are found.
   * @param flag flag to be set
   * @return self reference
   */
  public TextInput validate(final boolean flag) {
    validate = flag;
    decoder.validate = flag;
    return this;
  }

  /**
   * Sets a new encoding.
   * @param encoding encoding (ignored if {@code null} or an empty string)
   * @return self reference
   * @throws IOException I/O Exception
   */
  public TextInput encoding(final String encoding) throws IOException {
    if(encoding != null && !encoding.isEmpty()) {
      final String enc = normEncoding(encoding, false);
      decoder = TextDecoder.get(enc != UTF16 ? enc :
        decoder.encoding.equals(UTF16LE) ? UTF16LE : UTF16BE);
      decoder.validate = validate;
    }
    return this;
  }

  /**
   * Returns the next codepoint.
   * @return next codepoint
   * @throws IOException I/O exception
   */
  @Override
  public int read() throws IOException {
    final int cp = decoder.read(this);
    if(cp != -1 && !XMLToken.valid(cp)) {
      if(validate) throw new InputException(cp);
      return Token.REPLACEMENT;
    }
    return cp;
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
    try {
      final TokenBuilder tb = new TokenBuilder(Array.initialCapacity(length));
      for(int ch; (ch = read()) != -1;) tb.add(ch);
      return tb;
    } finally {
      close();
    }
  }
}
