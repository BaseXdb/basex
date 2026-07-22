package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.util.list.*;

/**
 * This class provides functions for encoding and decoding Base64 strings.
 *
 * @author BaseX Team, BSD License
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
    return java.util.Base64.getEncoder().encodeToString(token(token));
  }

  /**
   * Encodes the specified token.
   * @param token value to be encoded
   * @return resulting token
   */
  public static byte[] encode(final byte[] token) {
    return java.util.Base64.getEncoder().encode(token);
  }

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
    final int l = s.length;
    if((l & 3) != 0) throw error(s);
    if(l != 0 && s[l - 1] == '=') {
      if(s[l - 2] == '=') {
        if(!contains(ENDING, s[l - 3])) throw error(substring(s, l - 3));
      } else {
        if(!contains(ENDING2, s[l - 2])) throw error(substring(s, l - 4));
      }
    }
    return java.util.Base64.getDecoder().decode(s);
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
