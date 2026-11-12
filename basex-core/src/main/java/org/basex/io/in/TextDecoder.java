package org.basex.io.in;

import static org.basex.util.Strings.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * Decoder for input codepoints: UTF-8 is decoded by hand, all other encodings by Java's charset decoders.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class TextDecoder {
  /** Encoding. */
  final String encoding;
  /** Replace invalid input with Unicode replacement character. */
  boolean fallback = true;

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
   * Checks if the specified input is encoded as valid UTF-8 and needs no decoding.
   * @param bytes input
   * @return result of check
   */
  boolean adoptable(final byte[] bytes) {
    return bytes.length == 0;
  }

  /**
   * Returns a decoder for the specified encoding.
   * @param encoding normed encoding
   * @return decoder
   * @throws IOException I/O exception
   */
  static TextDecoder get(final String encoding) throws IOException {
    return encoding.equals(UTF8) ? new UTF8() : new Generic(encoding);
  }

  /**
   * Throws an exception for an incomplete codepoint or returns the replacement character (\\uFFFD).
   * @param incomplete add placeholder for missing byte
   * @param bytes bytes
   * @return replacement codepoint
   * @throws IOException I/O exception
   */
  final int invalid(final boolean incomplete, final byte... bytes) throws IOException {
    if(fallback) return Token.REPLACEMENT;

    final TokenBuilder tb = new TokenBuilder();
    final IntUnaryOperator toHex = c -> c + (c > 9 ? '7' : '0');
    for(final int b : bytes) {
      if(!tb.isEmpty()) tb.add(", ");
      tb.add(toHex.applyAsInt(b >> 4 & 0x0F)).add(toHex.applyAsInt(b & 0x0F));
    }
    if(incomplete) tb.add(", ??");
    throw new DecodingException("Invalid " + encoding + " character encoding: " + tb);
  }

  /** UTF8 Decoder. */
  static final class UTF8 extends TextDecoder {
    /** Bytes of the current codepoint. */
    private final byte[] cpBytes = new byte[4];

    /** Constructor. */
    UTF8() {
      super(UTF8);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      final int cp = ti.readByte();
      if(cp < 0x80) return cp;
      if(cp < 0xC2 || cp > 0xF4) return invalid(false, (byte) cp);
      final int cl = Token.cl((byte) cp);
      final byte[] bytes = cpBytes;
      bytes[0] = (byte) cp;
      for(int c = 1; c < cl; c++) {
        final int b = ti.readByte();
        if(b < 0) return invalid(true, Arrays.copyOf(bytes, c));
        bytes[c] = (byte) b;
        if(!continuation(cp, c, b)) {
          // keep the byte: it may start the next character
          ti.unreadByte();
          return invalid(false, Arrays.copyOf(bytes, c + 1));
        }
      }
      return Token.cp(bytes, 0);
    }

    /**
     * Checks if a byte is a valid continuation byte (rejecting overlongs, surrogates, >U+10FFFF).
     * @param first first byte of the sequence
     * @param c position of the byte in the sequence
     * @param b byte
     * @return result of check
     */
    private static boolean continuation(final int first, final int c, final int b) {
      final boolean second = c == 1;
      final int min = second && first == 0xE0 ? 0xA0 : second && first == 0xF0 ? 0x90 : 0x80;
      final int max = second && first == 0xED ? 0x9F : second && first == 0xF4 ? 0x8F : 0xBF;
      return b >= min && b <= max;
    }

    @Override
    boolean adoptable(final byte[] bytes) {
      final int bl = bytes.length;
      for(int p = 0; p < bl;) {
        final byte b = bytes[p];
        // negative bytes start a multi-byte sequence
        if(b >= 0) {
          if(b == 0) return false;
          p++;
        } else {
          final int cp = b & 0xFF;
          if(cp < 0xC2 || cp > 0xF4) return false;
          final int e = p + Token.cl(b);
          if(e > bl) return false;
          for(int c = p + 1; c < e; c++) {
            if(!continuation(cp, c - p, bytes[c] & 0xFF)) return false;
          }
          if(!XMLToken.valid11(Token.cp(bytes, p))) return false;
          p = e;
        }
      }
      return true;
    }
  }

  /** Generic decoder, delegating to Java's charset decoders. */
  private static final class Generic extends TextDecoder {
    /** Input buffer (undecoded bytes). */
    private final ByteBuffer in = ByteBuffer.allocate(16);
    /** Output buffer (decoded but not yet emitted characters). */
    private final CharBuffer out = CharBuffer.allocate(8);
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
      out.limit(0);
    }

    @Override
    int read(final TextInput ti) throws IOException {
      if(out.hasRemaining()) return codePoint();

      while(true) {
        final int b = ti.readByte();
        final boolean eof = b < 0;
        if(!eof) in.put((byte) b);
        in.flip();
        out.clear();
        final CoderResult cr = csd.decode(in, out, eof);
        if(eof && !cr.isError()) {
          csd.flush(out);
          // reset the decoder so it stays usable if read() is called again after EOF
          csd.reset();
        }
        out.flip();
        if(out.hasRemaining()) {
          in.compact();
          return codePoint();
        }
        if(cr.isError()) {
          final byte[] bytes = new byte[cr.length()];
          in.get(bytes);
          in.compact();
          return invalid(false, bytes);
        }
        if(eof) {
          final int rem = in.remaining();
          final byte[] bytes = new byte[rem];
          in.get(bytes);
          in.compact();
          return rem == 0 ? -1 : invalid(true, bytes);
        }
        in.compact();
      }
    }

    /**
     * Returns the next codepoint from the output buffer.
     * @return codepoint
     */
    private int codePoint() {
      final char ch = out.get();
      return Character.isHighSurrogate(ch) && out.hasRemaining() ?
        Character.toCodePoint(ch, out.get()) : ch;
    }
  }
}
