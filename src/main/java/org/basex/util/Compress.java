package org.basex.util;

import org.basex.util.list.*;

/**
 * This class compresses and decompresses tokens. It is inspired by the
 * Huffman coding, but was simplified to speed up processing.
 *
 * NOTE: this class is not thread-safe.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Wolfgang Kronberg
 */
public final class Compress {
  /** A ByteList instance serving as a buffer. */
  private final MyByteList bl = new MyByteList();
  /** Temporary value. */
  private int pc;
  /** Pack offset. */
  private int po;
  /** Current unpack position. */
  private int uc;
  /** Unpack offset. */
  private int uo;

  /**
   * Compresses the specified text.
   * @param txt text to be packed
   * @return packed text
   */
  public byte[] pack(final byte[] txt) {
    // initialize compression
    final int tl = txt.length;
    bl.reset();
    Num.set(bl.get(), tl, 0);
    bl.size(Num.length(tl));
    pc = 0;
    po = 0;

    // write packer version bit (0)
    push(0, 1);

    // relate upper with lower case and write mapping bit
    int lc = 0;
    for(final byte b : txt) lc += b >= 'A' && b <= 'Z' ? -1 : 1;
    final byte[] pack = lc >= 0 ? PACK1 : PACK2;
    push(lc >= 0 ? 1 : 0, 1);

    // compress all characters
    for(final byte t : txt) {
      int b = t;
      if(b >= 0) b = pack[b];
      if(b >= 0x00 && b < 0x08) { // 1 xxx
        push(1 | b << 1, 4);
      } else if(b >= 0x08 && b < 0x10) { // 01 xxx
        push(2 | b << 2, 5);
      } else if(b >= 0x10 && b < 0x20) { // 001 xxxx
        push(4 | b << 3, 7);
      } else if(b >= 0x20 && b < 0x40) { // 0001 xxxxx
        push(8 | b << 4, 9);
      } else { // 0000 xxxxxxxx
        push(b << 4, 12);
      }
    }
    if(po != 0) bl.add(pc);
    return bl.size() < tl ? bl.toArray() : txt;
  }

  /**
   * Pushes bits to the byte cache.
   * @param b value to be pushed.
   * @param s number of bits
   */
  private void push(final int b, final int s) {
    int bb = b, oo = po, cc = pc;
    for(int i = 0; i < s; i++) {
      cc |= (bb & 1) << oo;
      bb >>= 1;
      if(++oo == 8) {
        bl.add(cc);
        oo = 0;
        cc = 0;
      }
    }
    po = oo;
    pc = cc;
  }

  /**
   * Decompresses the specified text.
   * @param txt text to be unpacked
   * @return unpacked text
   */
  public byte[] unpack(final byte[] txt) {
    // initialize decompression
    final byte[] tmp = bl.get();
    bl.set(txt);
    uc = Num.length(txt, 0);
    uo = 0;

    // read packer bit
    pull();
    // choose mapping
    final byte[] unpack = pull() ? UNPACK1 : UNPACK2;

    // decompress all characters
    final int l = Num.get(txt, 0);
    final byte[] res = new byte[l];
    for(int r = 0; r < l; r++) {
      final int b;
      if(pull()) { // 1 xxx
        b = pull(3);
      } else if(pull()) { // 01 xxx
        b = pull(3) | 0x08;
      } else if(pull()) { // 001 xxxx
        b = pull(4) | 0x10;
      } else if(pull()) { // 0001 xxxxx
        b = pull(5) | 0x20;
      } else { // 0000 xxxxxxxx
        b = pull(8);
      }
      res[r] = (byte) (b >= 128 ? b : unpack[b]);
    }
    // make sure that the external txt byte array does not remain in this class
    bl.set(tmp);
    return res;
  }

  /**
   * Pulls the specified number of bits and returns the result.
   * @param s number of bytes
   * @return result
   */
  private int pull(final int s) {
    int oo = uo, cc = uc, x = 0;
    final byte[] l = bl.get();
    for(int i = 0; i < s; i++) {
      if((l[cc] & 1 << oo) != 0) x |= 1 << i;
      if(++oo == 8) {
        oo = 0;
        ++cc;
      }
    }
    uo = oo;
    uc = cc;
    return x;
  }

  /**
   * Pulls a single bit.
   * @return result
   */
  private boolean pull() {
    int oo = uo;
    final boolean b = (bl.get()[uc] & 1 << oo) != 0;
    if(++oo == 8) {
      oo = 0;
      ++uc;
    }
    uo = oo;
    return b;
  }

  /** First mapping for unpacking data. */
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
  /** First mapping for packing data. */
  private static final byte[] PACK1 = new byte[UNPACK1.length];

  /** Second mapping for unpacking data. */
  private static final byte[] UNPACK2 = new byte[UNPACK1.length];
  /** Second mapping for packing data. */
  private static final byte[] PACK2 = new byte[UNPACK2.length];

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

  /** Local ByteList implementation to make protected fields accessible. */
  static final class MyByteList extends ByteList {
    /**
     * Exchanges the actual byte array backing this list instance.
     * @param newList the new value for ByteList.list, including
     *   setting ByteList.size to list.size.
     */
    void set(final byte[] newList) {
      list = newList;
      size = newList.length;
    }

    /**
     * Direct access to the backing byte array.
     * @return ByteList.list
     */
    byte[] get() {
      return list;
    }

    /**
     * Sets the list size to a new value.
     * @param newSize the new value for ByteList.size
     */
    void size(final int newSize) {
      size = newSize;
    }
  }
}
