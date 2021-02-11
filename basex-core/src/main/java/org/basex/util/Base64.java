package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.util.list.*;

/**
 * This class provides functions for encoding and decoding Base64 strings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Base64 {
  /** Ending characters. */
  private static final byte[] ENDING = token("AQgw");
  /** Ending characters. */
  private static final byte[] ENDING2 = token("AEIMQUYcgkosw048");

  /** Hidden constructor. */
  private Base64() { }

  /**
   * Encodes the specified string.
   * @param token value to be encoded
   * @return resulting token
   */
  public static String encode(final String token) {
    return string(encode(token(token)));
  }

  /**
   * Encodes the specified token.
   * @param token value to be encoded
   * @return resulting token
   */
  public static byte[] encode(final byte[] token) {
    final ByteList bl = new ByteList();
    final int a = token.length;
    final int f = a / 3;
    final int p = a - 3 * f;

    int c = 0;
    for(int i = 0; i < f; ++i) {
      final int b0 = token[c++] & 0xff;
      final int b1 = token[c++] & 0xff;
      final int b2 = token[c++] & 0xff;
      bl.add(H2B[b0 >> 2]);
      bl.add(H2B[b0 << 4 & 0x3f | b1 >> 4]);
      bl.add(H2B[b1 << 2 & 0x3f | b2 >> 6]);
      bl.add(H2B[b2 & 0x3f]);
    }

    if(p != 0) {
      final int b0 = token[c++] & 0xff;
      bl.add(H2B[b0 >> 2]);
      if(p == 1) {
        bl.add(H2B[b0 << 4 & 0x3f]);
        bl.add('=');
        bl.add('=');
      } else {
        final int b1 = token[c] & 0xff;
        bl.add(H2B[b0 << 4 & 0x3f | b1 >> 4]);
        bl.add(H2B[b1 << 2 & 0x3f]);
        bl.add('=');
      }
    }
    return bl.finish();
  }

  /** Hex to byte conversion table. */
  private static final byte[] H2B = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
    'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
    'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  /**
   * Decodes the specified string.
   * @param token value to be decoded
   * @return resulting token
   */
  public static String decode(final String token) {
    return string(decode(token(token)));
  }

  /**
   * Decodes the specified token.
   * @param token value to be decoded
   * @return resulting token
   */
  public static byte[] decode(final byte[] token) {
    final ByteList bl = new ByteList();
    for(final byte c : token) {
      if(c < 0 || c > ' ') bl.add(c);
    }
    final byte[] s = bl.finish();

    // input must be a multiple of four characters
    if((s.length & 3) != 0) throw error(s);

    final int l = s.length, g = l >>> 2;
    int m = 0, n = g;
    if(l != 0) {
      if(s[l - 1] == '=') {
        ++m;
        --n;
      }
      if(s[l - 2] == '=') {
        ++m;
        if(!contains(ENDING, s[l - 3])) throw error(substring(s, l - 3));
      }
      if(m == 1 && !contains(ENDING2, s[l - 2])) throw error(substring(s, l - 4));
    }

    final byte[] val = new byte[3 * g - m];
    int c = 0, o = 0;
    for(int i = 0; i < n; ++i) {
      final int c0 = b2h(s[c++]), c1 = b2h(s[c++]), c2 = b2h(s[c++]), c3 = b2h(s[c++]);
      val[o++] = (byte) (c0 << 2 | c1 >> 4);
      val[o++] = (byte) (c1 << 4 | c2 >> 2);
      val[o++] = (byte) (c2 << 6 | c3);
    }

    if(m != 0) {
      final int c0 = b2h(s[c++]), c1 = b2h(s[c++]);
      val[o++] = (byte) (c0 << 2 | c1 >> 4);
      if(m == 1) val[o] = (byte) (c1 << 4 | b2h(s[c]) >> 2);
    }
    return val;
  }

  /** Byte to hex conversion table. */
  private static final byte[] B2H = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
    -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
    -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
    20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31,
    32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
    50, 51
  };

  /**
   * Converts a single byte to its hex representation.
   * @param c character to be encoded
   * @return encoded value
   */
  private static int b2h(final byte c) {
    if(c < 0 || c >= B2H.length) throw error((char) c);
    final int result = B2H[c];
    if(result < 0) throw error((char) c);
    return result;
  }

  /**
   * Throws an illegal argument exception for the specified character.
   * @param a character
   * @return error
   */
  private static IllegalArgumentException error(final Object a) {
    return new IllegalArgumentException(Util.info("Invalid Base64 cast: %.", a));
  }
}
