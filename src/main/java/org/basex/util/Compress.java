package org.basex.util;

/**
 * This class compresses and decompresses tokens. It is inspired by the
 * Huffman coding, but was simplified to speed up processing.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Compress {
  /** Byte list. */
  private final ByteList bl = new ByteList();
  /** Minimum compression size. */
  private final int min;
  /** Temporary value, or current position. */
  private int c;
  /** Offset. */
  private int o;

  /**
   * Default constructor.
   */
  public Compress() {
    this(7);
  }

  /**
   * Constructor, specifying a minimum token length for compression.
   * @param m minimum token length ({@code 0}: compress all)
   */
  public Compress(final int m) {
    min = m;
  }
  
  /**
   * Compresses the specified text.
   * @param txt text to be packed
   * @return packed text
   */
  public byte[] pack(final byte[] txt) {
    // skip short texts
    final int tl = txt.length;
    if(tl <= min) return txt;
    
    bl.reset();
    Num.write(bl.list, tl, 0);
    bl.size = Num.len(tl);
    c = 0;
    o = 0;

    for(int t = 0; t < tl; t++) {
      int b = txt[t];
      if(b >= 0) b = PACK[b];
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
    if(o != 0) bl.add(c);
    return bl.size() > tl ? txt : bl.toArray();
  }
  
  /**
   * Pushes bits to the byte cache.
   * @param b value to be pushed.
   * @param s number of bytes
   */
  private void push(final int b, final int s) {
    int bb = b, oo = o, cc = c;
    for(int i = 0; i < s; i++) {
      cc |= (bb & 1) << oo;
      bb >>= 1;
      if(++oo == 8) {
        bl.add(cc);
        oo = 0;
        cc = 0;
      }
    }
    o = oo;
    c = cc;
  }
  
  /**
   * Decompresses the specified text.
   * @param txt text to be unpacked
   * @return unpacked text
   */
  public byte[] unpack(final byte[] txt) {
    bl.list = txt;
    bl.size = txt.length;
    c = Num.len(txt, 0);
    o = 0;
    
    final int l = Num.read(txt, 0);
    final byte[] res = new byte[l];
    for(int r = 0; r < l; r++) {
      int b = 0;
      if(pull(1) != 0) { // 1 xxx
        b = pull(3);
      } else if(pull(1) != 0) { // 01 xxx
        b = pull(3) | 0x08;
      } else if(pull(1) != 0) { // 001 xxxx
        b = pull(4) | 0x10;
      } else if(pull(1) != 0) { // 0001 xxxxx
        b = pull(5) | 0x20;
      } else { // 0000 xxxxxxxx
        b = pull(8);
      }
      res[r] = (byte) (b >= 128 ? b : UNPACK[b]);
    }
    return res;
  }

  /**
   * Pulls the specified number of bytes and returns the result.
   * @param s number of bytes
   * @return result
   */
  private int pull(final int s) {
    int oo = o, cc = c, x = 0;
    for(int i = 0; i < s; i++) {
      if((bl.list[cc] & 1 << oo) != 0) x |= 1 << i;
      if(++oo == 8) {
        oo = 0;
        cc++;
      }
    }
    o = oo;
    c = cc;
    return x;
  }

  /** Mapping for packing data. */
  private static final byte[] PACK;
  /** Mapping for unpacking data. */
  private static final byte[] UNPACK = {
    0x20, 0x61, 0x65, 0x6E, 0x69, 0x6F, 0x72, 0x73, // encode via 1 xxx
    0x74, 0x6C, 0x75, 0x68, 0x64, 0x63, 0x67, 0x6D, // encode via 01 xxx
    0x70, 0x79, 0x62, 0x6B, 0x66, 0x76, 0x43, 0x53, // encode via 001 xxxx
    0x77, 0x4D, 0x41, 0x42, 0x50, 0x7A, 0x2E, 0x0A,
    0x54, 0x52, 0x4B, 0x4C, 0x47, 0x4E, 0x48, 0x6A, // encode via 0001 xxxxx
    0x45, 0x49, 0x44, 0x46, 0x4A, 0x78, 0x4F, 0x71, 
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
    0x38, 0x39, 0x3A, 0x2D, 0x27, 0x2C, 0x22, 0x3F,
    0x56, 0x57, 0x55, 0x5A, 0x59, 0x51, 0x58, 0x40,
    0x0D, 0x28, 0x2F, 0x29, 0x2B, 0x7E, 0x21, 0x23, // encode via 0000 xxxxxxxx
    0x24, 0x25, 0x26, 0x2A, 0x3B, 0x3C, 0x3D, 0x3E,
    0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 0x60, 0x7B, 0x7C,
    0x7D, 0x7F, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
    0x06, 0x07, 0x08, 0x09, 0x0B, 0x0C, 0x0E, 0x0F,
    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
    0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F
  };

  // initializes the character mapping
  static {
    final int pl = UNPACK.length;
    PACK = new byte[pl];
    for(int p = 0; p < pl; p++) PACK[UNPACK[p]] = (byte) p; 
  }
}
