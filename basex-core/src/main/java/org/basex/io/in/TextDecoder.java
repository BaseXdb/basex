package org.basex.io.in;

import static org.basex.util.Strings.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * This abstract class specifies a single method for decoding input to UTF-8.
 * The inheriting classes are optimized for performance and faster than Java's
 * default decoders.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class TextDecoder {
  /** Encoding. */
  final String encoding;
  /** Indicates if input must be valid. */
  boolean validate;

  /**
   * Constructor.
   * @param encoding encoding
   */
  TextDecoder(final String encoding) {
    this.encoding = encoding;
  }

  /**
   * Returns the next codepoint.
   * @param ti text input
   * @return next codepoint
   * @throws IOException I/O exception
   */
  abstract int read(TextInput ti) throws IOException;

  /**
   * Returns a decoder for the specified encoding.
   * @param encoding normed encoding
   * @return decoder
   * @throws IOException I/O exception
   */
  static TextDecoder get(final String encoding) throws IOException {
    return switch(encoding) {
      case UTF8 -> new UTF8();
      case UTF32 -> new UTF32();
      case UTF16LE -> new UTF16LE();
      case UTF16, UTF16BE -> new UTF16BE();
      default -> new Generic(encoding);
    };
  }

  /**
   * Reads a UTF-16 codepoint.
   * @param ti text input
   * @param be low/big endian
   * @return codepoint
   * @throws IOException I/O exception
   */
  final int readUTF16(final TextInput ti, final boolean be) throws IOException {
    final int a = ti.readByte();
    if(a < 0) return a;
    final int b = ti.readByte();
    if(b < 0) return invalid(true, (byte) a);

    final int cp = be ? a << 8 | b : a | b << 8;
    if(cp < 0xD800 || cp > 0xDFFF) return cp;
    if(cp >= 0xDC00 && cp <= 0xDFFF) return invalid(true, (byte) a, (byte) b);

    final int c = ti.readByte();
    if(c < 0) return invalid(true, (byte) a, (byte) b);
    final int d = ti.readByte();
    if(d < 0) return invalid(true, (byte) a, (byte) b, (byte) c);

    final int l = be ? c << 8 | d : c | d << 8;
    if(l < 0xDC00 || l > 0xDFFF) return invalid(true, (byte) a, (byte) b, (byte) c, (byte) d);
    return 0x10000 + ((cp & 0x3FF) << 10) + (l & 0x3FF);
  }

  /**
   * Throws an exception for an incomplete codepoint or returns the replacement character (\\uFFFD).
   * @param incomplete add placeholder for missing byte
   * @param bytes bytes
   * @return replacement codepoint
   * @throws IOException I/O exception
   */
  final int invalid(final boolean incomplete, final byte... bytes) throws IOException {
    if(validate) {
      final TokenBuilder tb = new TokenBuilder();
      final IntUnaryOperator toHex = c -> c + (c > 9 ? '7' : '0');
      for(final int b : bytes) {
        if(!tb.isEmpty()) tb.add(", ");
        tb.add(toHex.applyAsInt(b >> 4 & 0x0F)).add(toHex.applyAsInt(b & 0x0F));
      }
      if(incomplete) tb.add(", ??");
      throw new DecodingException("Invalid " + encoding + " character encoding: " + tb);
    }
    return Token.REPLACEMENT;
  }

  /** UTF8 Decoder. */
  private static final class UTF8 extends TextDecoder {
    /** Constructor. */
    UTF8() {
      super(UTF8);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      int cp = ti.readByte();
      if(cp < 0x80) return cp;
      if(cp < 0xC2 || cp > 0xF4) return invalid(false, (byte) cp);
      final int cl = Token.cl((byte) cp);
      final byte[] bytes = new byte[cl];
      bytes[0] = (byte) cp;
      for(int c = 1; c < cl; ++c) {
        cp = ti.readByte();
        bytes[c] = (byte) cp;
        if(cp < 0x80) return invalid(cp < 0, Arrays.copyOf(bytes, cp < 0 ? c : c + 1));
      }
      return Token.cp(bytes, 0);
    }
  }

  /** UTF16LE Decoder. */
  private static final class UTF16LE extends TextDecoder {
    /** Constructor. */
    UTF16LE() {
      super(UTF16LE);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      return readUTF16(ti, false);
    }
  }

  /** UTF16BE Decoder. */
  private static final class UTF16BE extends TextDecoder {
    /** Constructor. */
    UTF16BE() {
      super(UTF16BE);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      return readUTF16(ti, true);
    }
  }

  /** UTF32 Decoder. */
  private static final class UTF32 extends TextDecoder {
    /** Constructor. */
    UTF32() {
      super(UTF32);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      final int a = ti.readByte();
      if(a < 0) return a;
      final int b = ti.readByte();
      if(b < 0) return invalid(true, (byte) a);
      final int c = ti.readByte();
      if(c < 0) return invalid(true, (byte) a, (byte) b);
      final int d = ti.readByte();
      if(d < 0) return invalid(true, (byte) a, (byte) b, (byte) c);
      return a << 24 | b << 16 | c << 8 | d;
    }
  }

  /** Generic Decoder. */
  private static final class Generic extends TextDecoder {
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
     * @param encoding encoding
     * @throws IOException I/O exception
     */
    private Generic(final String encoding) throws IOException {
      super(encoding);
      try {
        csd = Charset.forName(encoding).newDecoder();
      } catch(final Exception ex) {
        throw new DecodingException(ex);
      }
    }

    @Override
    int read(final TextInput ti) throws IOException {
      int c = -1;
      while(++c < 4) {
        final int a = ti.readByte();
        if(a < 0) break;

        cache[c] = (byte) a;
        outc.position(0);
        inc.position(0);
        inc.limit(c + 1);
        csd.reset();
        final CoderResult cr = csd.decode(inc, outc, true);
        if(cr.isMalformed()) continue;

        // return codepoint
        int i = 0;
        final int os = outc.position();
        for(int o = 0; o < os; ++o) i |= outc.get(o) << (o << 3);
        return i;
      }
      return c == 0 ? -1 : invalid(false, cache[0]);
    }
  }
}
