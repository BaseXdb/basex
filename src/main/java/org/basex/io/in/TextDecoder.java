package org.basex.io.in;

import static org.basex.util.Token.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;

/**
 * This abstract class specifies a single method for decoding input to UTF-8.
 * The inheriting classes are optimized for performance and faster than Java's
 * default decoders.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class TextDecoder {
  /**
   * Returns the next character.
   * @param ti text input
   * @return next character
   * @throws IOException I/O exception
   */
  abstract int read(final TextInput ti) throws IOException;

  /**
   * Returns a decoder for the specified encoding.
   * @param enc encoding
   * @return decoder
   * @throws IOException I/O exception
   */
  static TextDecoder get(final String enc) throws IOException {
    if(enc == UTF8)    return new TextDecoder.UTF8();
    if(enc == UTF32)   return new TextDecoder.UTF32();
    if(enc == UTF16LE) return new TextDecoder.UTF16LE();
    if(enc == UTF16BE) return new TextDecoder.UTF16BE();
    return new TextDecoder.Generic(enc);
  }

  /**
   * Processes an invalid character.
   * @throws IOException I/O exception
   * @return exception
   */
  MalformedInputException invalid() throws IOException {
    throw new MalformedInputException(0);
  }

  /** UTF8 Decoder. */
  static class UTF8 extends TextDecoder {
    /** UTF8 cache. */
    private final byte[] cache = new byte[4];

    @Override
    int read(final TextInput ti) throws IOException {
      int ch = ti.next();
      if(ch < 0x80) return ch;
      cache[0] = (byte) ch;
      final int cl = cl((byte) ch);
      for(int c = 1; c < cl; ++c) {
        ch = ti.next();
        if(ch < 0) invalid();
        cache[c] = (byte) ch;
      }
      return cp(cache, 0);
    }
  }

  /** UTF16LE Decoder. */
  static class UTF16LE extends TextDecoder {
    @Override
    int read(final TextInput ti) throws IOException {
      final int a = ti.next();
      if(a < 0) return a;
      final int b = ti.next();
      if(b < 0) invalid();
      return a | b << 8;
    }
  }

  /** UTF16BE Decoder. */
  static class UTF16BE extends TextDecoder {
    @Override
    int read(final TextInput ti) throws IOException {
      final int a = ti.next();
      if(a < 0) return a;
      final int b = ti.next();
      if(b < 0) invalid();
      return a << 8 | b;
    }
  }

  /** UTF32 Decoder. */
  static class UTF32 extends TextDecoder {
    @Override
    int read(final TextInput ti) throws IOException {
      final int a = ti.next();
      if(a < 0) return a;
      final int b = ti.next();
      if(b < 0) invalid();
      final int c = ti.next();
      if(c < 0) invalid();
      final int d = ti.next();
      if(d < 0) invalid();
      return a << 24 | b << 16 | c << 8 | d;
    }
  }

  /** Generic Decoder. */
  static class Generic extends TextDecoder {
    /** Input cache. */
    private final byte[] cache = new byte[4];
    /** Input buffer. */
    private final ByteBuffer inc = ByteBuffer.wrap(cache);
    /** Output buffer. */
    private final CharBuffer outc = CharBuffer.wrap(new char[4]);
    /** Charset decoder. */
    private final CharsetDecoder csd;

    /**
     * Constructor.
     * @param enc encoding
     * @throws IOException I/O exception
     */
    Generic(final String enc) throws IOException {
      try {
        csd = Charset.forName(enc).newDecoder();
      } catch(final Exception ex) {
        throw new IOException(ex.toString());
      }
    }

    @Override
    int read(final TextInput ti) throws IOException {
      for(int c = 0; c < 4; c++) {
        final int ch = ti.next();
        if(ch < 0) return ch;
        cache[c] = (byte) ch;
        outc.position(0);
        inc.position(0);
        inc.limit(c + 1);
        csd.reset();
        final CoderResult cr = csd.decode(inc, outc, true);
        if(cr.isMalformed()) continue;
        // return character
        int i = 0;
        final int os = outc.position();
        for(int o = 0; o < os; ++o) i |= outc.get(o) << (o << 3);
        return i;
      }
      throw invalid();
    }
  }
}
