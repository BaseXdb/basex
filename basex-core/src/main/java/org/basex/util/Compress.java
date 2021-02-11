package org.basex.util;

import java.util.*;

import org.basex.data.*;

/**
 * This class compresses and decompresses tokens. It is inspired by the
 * Huffman coding, but was simplified to speed up processing.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Compress {
  /** Offset for compressing texts (see {@link DiskData}). */
  public static final long COMPRESS = 0x4000000000L;

  /** Private constructor. */
  private Compress() { }

  /**
   * Compresses the specified text. Returns the original text if the packed text is not shorter.
   * @param text text to be packed
   * @return packed or original text
   */
  public static byte[] pack(final byte[] text) {
    // only compress texts with more than 4 characters
    final int tl = text.length - 1;
    if(tl < 4) return text;

    // store length at beginning of array
    final byte[] bytes = new byte[tl];
    int size = Num.set(bytes, tl + 1);

    // find lower-case and non-ascii characters
    int lc = 0, uc = 0, out = 0;
    for(final byte t : text) {
      lc += t >= 'A' && t <= 'Z' ? -1 : 1;
      uc += t >= 0 ? 1 : -1;
    }
    // too many non-ascii characters: skip compression
    if(uc < 0) return text;

    // first bit: packer version (0), second bit: mapping type (upper/lower case)
    final byte[] map;
    if(lc >= 0) {
      out = 2;
      map = PACK1;
    } else {
      map = PACK2;
    }

    // loop through and compress all characters
    int in, off = 2;
    for(final byte t : text) {
      final int b = t >= 0 ? map[t] : t, s;
      if(b >= 0x00 && b < 0x08) { // 1 xxx
        in = 1 | b << 1;
        s = 4;
      } else if(b >= 0x08 && b < 0x10) { // 01 xxx
        in = 2 | b << 2;
        s = 5;
      } else if(b >= 0x10 && b < 0x20) { // 001 xxxx
        in = 4 | b << 3;
        s = 7;
      } else if(b >= 0x20 && b < 0x40) { // 0001 xxxxx
        in = 8 | b << 4;
        s = 9;
      } else { // 0000 xxxxxxxx
        in = b << 4;
        s = 12;
      }
      for(int i = 0; i < s; i++) {
        out |= (in & 1) << off;
        in >>>= 1;
        off = (off + 1) & 7;
        if(off == 0) {
          // skip compression if packed array gets too large
          if(size == tl) return text;
          bytes[size++] = (byte) out;
          out = 0;
        }
      }
    }
    if(off != 0) {
      // skip compression if packed array gets too large
      if(size == tl) return text;
      bytes[size++] = (byte) out;
    }
    return size < tl ? Arrays.copyOf(bytes, size) : bytes;
  }

  /**
   * Decompresses the specified text.
   * @param text compressed text
   * @return unpacked text
   */
  public static byte[] unpack(final byte[] text) {
    // bit position: skip stored length and packer bit
    int pos = (Num.length(text, 0) << 3) + 1;
    // choose mapping
    final byte[] map = isSet(text, pos++) ? UNPACK1 : UNPACK2;

    // decompress all characters
    final int size = Num.get(text, 0);
    final byte[] bytes = new byte[size];
    for(int b = 0; b < size; b++) {
      final int bits;
      int out = 0;
      if(isSet(text, pos++)) { // 1 xxx
        bits = 3;
      } else if(isSet(text, pos++)) { // 01 xxx
        bits = 3;
        out = 0x08;
      } else if(isSet(text, pos++)) { // 001 xxxx
        bits = 4;
        out = 0x10;
      } else if(isSet(text, pos++)) { // 0001 xxxxx
        bits = 5;
        out = 0x20;
      } else { // 0000 xxxxxxxx
        bits = 8;
      }
      for(int bit = 0; bit < bits; bit++, pos++) {
        if((text[pos >>> 3] & 1 << (pos & 7)) != 0) out |= 1 << bit;
      }
      bytes[b] = (byte) (out >= 0x80 ? out : map[out]);
    }
    return bytes;
  }

  /**
   * Indicates if the specified value is inlined.
   * @param value value
   * @return result of check
   */
  public static boolean compressed(final long value) {
    return (value & COMPRESS) != 0;
  }

  /**
   * Checks if a specified bit is set.
   * @param txt text to be unpacked
   * @param pos current position
   * @return result
   */
  private static boolean isSet(final byte[] txt, final int pos) {
    return (txt[pos >>> 3] & 1 << (pos & 7)) != 0;
  }

  /** First mapping for unpacking data (lower-case texts). */
  private static final byte[] UNPACK1 = {
    0x20, 0x61, 0x65, 0x6E, 0x69, 0x6F, 0x72, 0x73, // encode via 1 xxx
    0x74, 0x6C, 0x75, 0x68, 0x64, 0x63, 0x67, 0x6D, // encode via 01 xxx
    0x70, 0x79, 0x62, 0x6B, 0x66, 0x76, 0x43, 0x53, // encode via 001 xxxx
    0x77, 0x4D, 0x41, 0x42, 0x50, 0x7A, 0x2E, 0x0A,
    0x54, 0x52, 0x4B, 0x4C, 0x47, 0x4E, 0x48, 0x6A, // encode via 0001 xxxxx
    0x45, 0x49, 0x44, 0x46, 0x4A, 0x78, 0x4F, 0x71,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
    0x38, 0x39, 0x3A, 0x2D, 0x27, 0x2C, 0x22, 0x3F,
    0x56, 0x57, 0x55, 0x5A, 0x59, 0x51, 0x58, 0x09,
    0x40, 0x28, 0x2F, 0x29, 0x2B, 0x7E, 0x21, 0x23, // encode via 0000 xxxxxxxx
    0x24, 0x25, 0x26, 0x2A, 0x3B, 0x3C, 0x3D, 0x3E,
    0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 0x60, 0x7B, 0x7C,
    0x7D, 0x7F, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
    0x06, 0x07, 0x08, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
    0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F
  };
  /** Second mapping for unpacking data (upper-case texts). */
  private static final byte[] UNPACK2 = new byte[UNPACK1.length];
  /** First mapping for packing data (lower-case texts). */
  private static final byte[] PACK1 = new byte[UNPACK1.length];
  /** Second mapping for packing data (upper-case texts). */
  private static final byte[] PACK2 = new byte[UNPACK1.length];

  // initializes the character mappings
  static {
    final int pl = UNPACK1.length;
    for(int p = 0; p < pl; p++) {
      final byte b1 = UNPACK1[p];
      // swap lower and upper case in second mapping
      final byte b2 = (byte) (b1 >= 'A' && b1 <= 'Z' ? b1 + 0x20 :
        b1 >= 'a' && b1 <= 'z' ? b1 - 0x20 : b1);
      UNPACK2[p] = b2;
      PACK1[b1] = (byte) p;
      PACK2[b2] = (byte) p;
    }
  }
}
