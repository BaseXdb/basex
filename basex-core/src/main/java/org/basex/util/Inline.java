package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.data.*;

/**
 * This class converts tokens to a numeric value. The bytes of the inlined values are composed
 * as follows:
 *
 * <pre>
 * - Byte 0-2: unused
 * - Byte 3: see bit layout in {@link DiskData}.
 * - Byte 4: integer value or inlined whitespace token
 * </pre>
 *
 * XML 1.0 whitespaces will be represented as follows:
 *
 * <pre>
 * - 00: 0x0A (new line)
 * - 01: 0x09 (tabulator)
 * - 10: 0x20 (space)
 * - 11: 0x0D (carriage return)
 * </pre>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Inline {
  /** Offset for inlining values (see {@link DiskData}). */
  private static final long INLINE = 0x8000000000L;
  /** Offset for inlining strings (see {@link DiskData}). */
  private static final long STRING = 0x2000000000L;
  /** Whitespace mapping. */
  private static final byte[] WS = { 0x0A, 0x09, 0x20, 0x0d };

  /** Private constructor. */
  private Inline() { }

  /**
   * Creates a numeric (inlined) representation of the specified token.
   * @param token token to be inlined
   * @return inlined value, or {@code 0} if inlining is not possible
   */
  public static long pack(final byte[] token) {
    // inline integer value
    long value = packInt(token);
    if(value != -1) return value;

    // short token
    final long tl = token.length;
    if(tl <= 4) {
      value = INLINE | STRING | tl << 32;
      int c = 32;
      for(final byte b : token) value |= (b & 0xFFL) << (c -= 8);
      return value;
    }
    // compressed whitespace token
    if(tl < 16 && ws(token)) {
      value = INLINE | STRING | Compress.COMPRESS | tl << 32;
      int c = 32;
      // upper bit: 09/0A = 0, 0D/20 = 1
      // lower bit: 0A/20 = 0, 09/0D = 1
      for(final byte b : token) value |= (b < 0x0B ? 0L : 1L) << --c | (b & 1) << --c;
      return value;
    }
    // no inlining possible
    return 0;
  }

  /**
   * Converts the specified token into a positive integer value.
   * @param token token to be converted
   * @return inlined value, or {@code -1} if value cannot be inlined
   */
  public static long packInt(final byte[] token) {
    final int tl = token.length;
    // skip empty tokens
    if(tl == 0) return -1;
    // skip tokens starting with no digit or '0'
    if(tl > 1) {
      final byte b = token[0];
      if(b < '1' || b > '9') return -1;
    }

    long value = 0;
    for(final byte b : token) {
      value = (value << 3) + (value << 1) + b - '0';
      // skip conversion if byte is no digit, or if number is too large
      if(b < '0' || b > '9' || value > Integer.MAX_VALUE) return -1;
    }
    return INLINE | value;
  }

  /**
   * Converts an inlined value to a token.
   * @param value inlined value
   * @return unpacked token
   */
  public static byte[] unpack(final long value) {
    return (value & STRING) == 0 ? token((int) value) : unpackString(value);
  }

  /**
   * Converts an inlined value to a long value.
   * @param value inlined value
   * @return unpacked integer
   */
  public static long unpackLong(final long value) {
    return (value & STRING) == 0 ? (value & INLINE - 1) : toLong(unpackString(value));
  }

  /**
   * Converts an inlined value to a double value.
   * @param value inlined value
   * @return unpacked double
   */
  public static double unpackDouble(final long value) {
    return (value & STRING) == 0 ? (value & INLINE - 1) : toDouble(unpackString(value));
  }

  /**
   * Returns the token length of the inlined value.
   * @param value inlined value
   * @return length of unpacked token
   */
  public static int unpackLength(final long value) {
    return (value & STRING) == 0 ? numDigits((int) value) : (int) (value >> 32) & 0x0F;
  }

  /**
   * Indicates if the specified value is inlined.
   * @param value value
   * @return result of check
   */
  public static boolean inlined(final long value) {
    return (value & INLINE) != 0;
  }

  /**
   * Extracts an inlined string.
   * @param value inlined value
   * @return token
   */
  private static byte[] unpackString(final long value) {
    final int tl = (int) (value >> 32) & 0x0F, v = (int) value;
    final byte[] token = new byte[tl];
    if(tl <= 4) {
      // short token
      for(int t = 0, c = 24; t < tl; t++, c -= 8) token[t] = (byte) (v >> c);
    } else {
      // whitespace token
      for(int t = 0, c = 30; t < tl; t++, c -= 2) {
        final int o = v >> c;
        token[t] = WS[o & 2 | o & 1];
      }
    }
    return token;
  }
}
