package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on binary data.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class FNBin extends StandardFunc {
  /** Big endian order. */
  private static final byte[][] BIG = tokens("most-significant-first", "big-endian", "BE");
  /** Big endian order. */
  private static final byte[][] LITTLE = tokens("least-significant-first", "little-endian", "LE");

  /** Bit operations. */
  private enum Bit {
    /** Or.  */ OR,
    /** Xor. */ XOR,
    /** And. */ AND
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _BIN_TO_OCTETS: return toOctetsIter(qc);
      default:             return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _BIN_TO_OCTETS: return toOctets(qc);
      default:             return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _BIN_HEX:                     return hex(qc);
      case _BIN_BIN:                     return bin(qc);
      case _BIN_OCTAL:                   return octal(qc);
      case _BIN_FROM_OCTETS:             return fromOctets(qc);
      case _BIN_LENGTH:                  return length(qc);
      case _BIN_PART:                    return part(qc);
      case _BIN_JOIN:                    return join(qc);
      case _BIN_INSERT_BEFORE:           return insertBefore(qc);
      case _BIN_PAD_LEFT:                return pad(qc, true);
      case _BIN_PAD_RIGHT:               return pad(qc, false);
      case _BIN_FIND:                    return find(qc);
      case _BIN_DECODE_STRING:           return decodeString(qc);
      case _BIN_ENCODE_STRING:           return encodeString(qc);
      case _BIN_PACK_DOUBLE:             return packDouble(qc);
      case _BIN_PACK_FLOAT:              return packFloat(qc);
      case _BIN_PACK_INTEGER:            return packInteger(qc);
      case _BIN_UNPACK_DOUBLE:           return Dbl.get(unpack(qc, 8).getDouble());
      case _BIN_UNPACK_FLOAT:            return Flt.get(unpack(qc, 4).getFloat());
      case _BIN_UNPACK_INTEGER:          return unpackInteger(qc, true);
      case _BIN_UNPACK_UNSIGNED_INTEGER: return unpackInteger(qc, false);
      case _BIN_OR:                      return bit(Bit.OR, qc);
      case _BIN_XOR:                     return bit(Bit.XOR, qc);
      case _BIN_AND:                     return bit(Bit.AND, qc);
      case _BIN_NOT:                     return not(qc);
      case _BIN_SHIFT:                   return shift(qc);
      default:                           return super.item(qc, ii);
    }
  }

  /**
   * Converts a hexadecimal string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 hex(final QueryContext qc) throws QueryException {
    byte[] bytes = str(0, qc);
    if(bytes == null) return null;

    // add leading zero
    if((bytes.length & 1) != 0) bytes = concat(ZERO, bytes);
    try {
      return new B64(Hex.decode(bytes, info));
    } catch(final QueryException ex) {
      throw BIN_NNC.get(info);
    }
  }

  /**
   * Converts a binary string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 bin(final QueryContext qc) throws QueryException {
    final byte[] bytes = str(0, qc);
    if(bytes == null) return null;
    return new B64(binary2bytes(bytes));
  }

  /**
   * Converts an octal string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 octal(final QueryContext qc) throws QueryException {
    final byte[] bytes = str(0, qc);
    if(bytes == null) return null;
    final int bl = bytes.length;
    if(bl == 0) return new B64(EMPTY);

    try {
      byte[] bin = token(new BigInteger(string(bytes), 8).toString(2));
      final int expl = bl * 3;
      final int binl = bin.length;
      if(binl != expl) {
        // add leading zeroes
        final byte[] tmp = new byte[expl];
        Arrays.fill(tmp, 0, expl - binl, (byte) '0');
        System.arraycopy(bin, 0, tmp, expl - binl, binl);
        bin = tmp;
      }
      return new B64(binary2bytes(bin));
    } catch(final NumberFormatException ex) {
      throw BIN_NNC.get(info);
    }
  }

  /**
   * Returns a binary string to a byte array.
   * @param bytes binary string
   * @return byte array
   * @throws QueryException query exception
   */
  private byte[] binary2bytes(final byte[] bytes) throws QueryException {
    final int bl = bytes.length;
    final int r = bl & 7, l = 8 - r & 7, s = bl + 7 >>> 3;
    final byte[] tmp = new byte[s];
    for(int i = 0; i < bl; i++) {
      final byte b = bytes[i];
      if(b == '1') {
        tmp[l + i >>> 3] |= 0x80 >>> (i - r & 7);
      } else if(b != '0') {
        throw BIN_NNC.get(info);
      }
    }
    return tmp;
  }

  /**
   * Converts binary data to integers.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value toOctets(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    final byte[] bytes = b64.binary(info);
    final long[] vals = new long[bytes.length];
    for(int i = 0; i < bytes.length; i++) vals[i] = bytes[i] & 0xFF;
    return IntSeq.get(vals, AtomType.ITR);
  }

  /**
   * Converts binary data to integers.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter toOctetsIter(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    final byte[] bytes = b64.binary(info);
    return new ValueIter() {
      final int s = bytes.length;
      int c;
      @Override
      public Int get(final long i) { return Int.get(bytes[(int) i] & 0xFF); }
      @Override
      public Int next() { return c < s ? get(c++) : null; }
      @Override
      public boolean reset() { c = 0; return true; }
      @Override
      public long size() { return s; }
      @Override
      public Value value() {
        final long[] vals = new long[bytes.length];
        for(int i = 0; i < bytes.length; i++) vals[i] = bytes[i];
        return IntSeq.get(vals, AtomType.ITR);
      }
    };
  }

  /**
   * Converts integers to binary data.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 fromOctets(final QueryContext qc) throws QueryException {
    final AtomIter ir = exprs[0].atomIter(qc, info);
    final ByteList bl = new ByteList(Math.max(Array.CAPACITY, (int) ir.size()));
    for(Item it; (it = ir.next()) != null;) {
      final long l = toLong(it);
      if(l < 0 || l > 255) throw BIN_OOR_X.get(info, l);
      bl.add((int) l);
    }
    return new B64(bl.finish());
  }

  /**
   * Returns the length of binary array.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Int length(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    return Int.get(b64.binary(info).length);
  }

  /**
   * Returns part of a binary array.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 part(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final Long len = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    final byte[] tmp = new byte[bounds[1]];
    System.arraycopy(bytes, bounds[0], tmp, 0, bounds[1]);
    return new B64(tmp);
  }

  /**
   * Joins binary data.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 join(final QueryContext qc) throws QueryException {
    final ByteList bl = new ByteList();
    final Iter ir = exprs[0].atomIter(qc, info);
    for(Item it; (it = ir.next()) != null;) bl.add(toB64(it, true).binary(info));
    return new B64(bl.finish());
  }

  /**
   * Inserts data into binary data.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 insertBefore(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final B64 xtr = toB64(exprs[2], qc, true);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, null, bl);

    if(xtr == null) return b64;
    final byte[] extra = xtr.binary(info);
    final int xl = extra.length;

    final byte[] tmp = new byte[bl + xl];
    final int o = bounds[0];
    System.arraycopy(bytes, 0, tmp, 0, o);
    System.arraycopy(extra, 0, tmp, o, xl);
    System.arraycopy(bytes, o, tmp, o + xl, bl - o);
    return new B64(tmp);
  }

  /**
   * Pads an array.
   * @param qc query context
   * @param left left/right flag
   * @return result
   * @throws QueryException query exception
   */
  private B64 pad(final QueryContext qc, final boolean left) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final long sz = toLong(exprs[1], qc);
    final long octet = exprs.length > 2 ? toLong(exprs[2], qc) : 0;
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    if(sz < 0) throw BIN_NS_X.get(info, sz);
    if(octet < 0 || octet > 255) throw BIN_OOR_X.get(info, octet);

    final byte[] tmp = new byte[(int) (bl + sz)];
    if(left) {
      Arrays.fill(tmp, 0, (int) sz, (byte) octet);
      System.arraycopy(bytes, 0, tmp, (int) sz, bl);
    } else {
      System.arraycopy(bytes, 0, tmp, 0, bl);
      Arrays.fill(tmp, bl, tmp.length, (byte) octet);
    }
    return new B64(tmp);
  }

  /**
   * Finds the first occurrence of binary data.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Int find(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final Long off = toLong(exprs[1], qc);
    final B64 srch = toB64(exprs[2], qc, false);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final byte[] search = srch.binary(info);
    final int[] bounds = bounds(off, null, bl);
    final int pos = indexOf(bytes, search, bounds[0]);
    return pos == -1 ? null : Int.get(pos);
  }

  /**
   * Converts binary data to a string.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Str decodeString(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    final String enc = toEncoding(1, BIN_UE_X, qc);
    final Long off = exprs.length > 2 ? toLong(exprs[2], qc) : null;
    final Long len = exprs.length > 3 ? toLong(exprs[3], qc) : null;
    if(b64 == null) return null;

    byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    if(bounds[0] > 0 || bounds[1] < bl) {
      final byte[] tmp = new byte[bounds[1]];
      System.arraycopy(bytes, bounds[0], tmp, 0, bounds[1]);
      bytes = tmp;
    }

    try {
      return Str.get(FNConvert.toString(new IOContent(bytes).inputStream(), enc, true));
    } catch(final IOException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }

  /**
   * Converts a string to binary data.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 encodeString(final QueryContext qc) throws QueryException {
    final byte[] str = str(0, qc);
    final String enc = toEncoding(1, BIN_UE_X, qc);
    if(str == null) return null;
    try {
      return new B64(enc == null || enc == UTF8 ? str : FNConvert.toBinary(str, enc));
    } catch(final CharacterCodingException ex) {
      throw BIN_CE_X.get(info, ex);
    }
  }

  /**
   * Packs a double.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packDouble(final QueryContext qc) throws QueryException {
    final double d = toDouble(exprs[0], qc);
    final ByteOrder bo = order(1, qc);
    return new B64(ByteBuffer.wrap(new byte[8]).order(bo).putDouble(d).array());
  }

  /**
   * Packs a float.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packFloat(final QueryContext qc) throws QueryException {
    final float f = toFloat(exprs[0], qc);
    final ByteOrder bo = order(1, qc);
    return new B64(ByteBuffer.wrap(new byte[4]).order(bo).putFloat(f).array());
  }

  /**
   * Packs an integer.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packInteger(final QueryContext qc) throws QueryException {
    long b = toLong(exprs[0], qc);
    final long sz = toLong(exprs[1], qc);
    final ByteOrder bo = order(2, qc);
    if(sz < 0) throw BIN_NS_X.get(info, sz);

    final byte[] tmp = new byte[(int) sz];
    final int tl = tmp.length;
    if(bo == ByteOrder.BIG_ENDIAN) {
      for(int t = tl - 1; t >= 0; t--) {
        tmp[t] = (byte) b;
        b >>= 8;
      }
    } else {
      for(int t = 0; t < tl; t++) {
        tmp[t] = (byte) b;
        b >>= 8;
      }
    }
    return new B64(tmp);
  }

  /**
   * Unpacks a double.
   * @param qc query context
   * @param len length of entry
   * @return result
   * @throws QueryException query exception
   */
  private ByteBuffer unpack(final QueryContext qc, final long len) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    final Long off = toLong(exprs[1], qc);
    final ByteOrder bo = order(2, qc);

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    final ByteBuffer bb = ByteBuffer.allocate(bounds[1]).order(bo);
    bb.put(bytes, bounds[0], bounds[1]).position(0);
    return bb;
  }

  /**
   * Unpacks an unsigned integer.
   * @param qc query context
   * @param signed signed flag
   * @return result
   * @throws QueryException query exception
   */
  private Int unpackInteger(final QueryContext qc, final boolean signed) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, false);
    final Long off = toLong(exprs[1], qc);
    final Long sz = toLong(exprs[2], qc);
    final ByteOrder bo = order(3, qc);

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, sz, bl);
    final int o = bounds[0], l = Math.min(8, bounds[1]);
    if(l == 0) return Int.get(0);

    // place input data in long byte array, consider sign
    final byte[] tmp = new byte[8];
    final boolean neg = signed && (bytes[0] & 0x80) != 0;
    if(bo == ByteOrder.BIG_ENDIAN) {
      final int s = 8 - l;
      if(neg) for(int i = 0; i < s; i++) tmp[i] = (byte) 0xFF;
      System.arraycopy(bytes, o, tmp, s, l);
    } else {
      System.arraycopy(bytes, o, tmp, 0, l);
      if(neg) for(int i = l; i < 8; i++) tmp[i] = (byte) 0xFF;
    }
    return Int.get(ByteBuffer.wrap(tmp).order(bo).getLong());
  }

  /**
   * Returns bitwise operations.
   * @param op bit operation
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 bit(final Bit op, final QueryContext qc) throws QueryException {
    final B64 b1 = toB64(exprs[0], qc, true);
    final B64 b2 = toB64(exprs[1], qc, true);
    if(b1 == null || b2 == null) return null;

    final byte[] bytes1 = b1.binary(info);
    final byte[] bytes2 = b2.binary(info);
    final int bl1 = bytes1.length;
    final int bl2 = bytes2.length;
    if(bl1 != bl2) throw BIN_DLA_X_X.get(info, bl1, bl2);

    final byte[] tmp = new byte[bl1];
    for(int b = 0; b < bl1; b++) {
      tmp[b] = (byte) (op == Bit.OR ? bytes1[b] | bytes2[b] :
        op == Bit.XOR ? bytes1[b] ^ bytes2[b] : bytes1[b] & bytes2[b]);
    }
    return new B64(tmp);
  }

  /**
   * Returns the bitwise not operation.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 not(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    final byte[] tmp = new byte[bl];
    for(int b = 0; b < bl; b++) tmp[b] = (byte) ~bytes[b];
    return new B64(tmp);
  }

  /**
   * Returns the bitwise shift operation.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 shift(final QueryContext qc) throws QueryException {
    final B64 b64 = toB64(exprs[0], qc, true);
    long by = toLong(exprs[1], qc);
    if(b64 == null) return null;
    if(by == 0) return b64;

    byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    byte[] tmp = new byte[bl];
    int r = 0;
    if(by > 7) {
      tmp = new BigInteger(bytes).shiftLeft((int) by).toByteArray();
      if(tmp.length != bl) {
        bytes = tmp;
        tmp = new byte[bl];
        System.arraycopy(bytes, bytes.length - bl, tmp, 0, bl);
      }
    } else if(by > 0) {
      for(int i = bl - 1; i >= 0; i--) {
        final byte b = bytes[i];
        tmp[i] = (byte) (b << by | r);
        r = b >>> 32 - by;
      }
    } else if(by > -8) {
      by = -by;
      for(int i = 0; i < bl; i++) {
        final int b = bytes[i] & 0xFF;
        tmp[i] = (byte) (b >>> by | r);
        r = b << 32 - by;
      }
    } else {
      by = -by;
      BigInteger bi = new BigInteger(bytes);
      if(bi.signum() >= 0) {
        bi = bi.shiftRight((int) by);
      } else {
        final BigInteger o = BigInteger.ONE.shiftLeft(bl * 8 + 1);
        final BigInteger m = o.subtract(BigInteger.ONE).shiftRight((int) by + 1);
        bi = bi.subtract(o).shiftRight((int) by).and(m);
      }
      tmp = bi.toByteArray();
      final int tl = tmp.length;
      if(tl != bl) {
        bytes = tmp;
        tmp = new byte[bl];
        System.arraycopy(bytes, 0, tmp, bl - tl, tl);
      }
    }
    return new B64(tmp);
  }

  // UTILITY FUNCTIONS ============================================================================

  /**
   * Converts the specified expression to a string.
   * Returns a token representation or an exception.
   * @param o expression offset
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] str(final int o, final QueryContext qc) throws QueryException {
    final Item it = exprs[o].atomItem(qc, info);
    return it == null ? null : toToken(it);
  }

  /**
   * Checks the octet order.
   * @param o expression offset
   * @param qc query context
   * @return order
   * @throws QueryException query exception
   */
  private ByteOrder order(final int o, final QueryContext qc) throws QueryException {
    if(o >= exprs.length) return ByteOrder.BIG_ENDIAN;
    final byte[] order = toToken(exprs[o], qc);
    if(eq(order, BIG)) return ByteOrder.BIG_ENDIAN;
    if(eq(order, LITTLE)) return ByteOrder.LITTLE_ENDIAN;
    throw BIN_USO_X.get(info, order);
  }

  /**
   * Checks the bounds of the specified offset value.
   * @param off offset value (may be {@code null})
   * @param len length value (may be {@code null})
   * @param sz size of input data
   * @return bounds
   * @throws QueryException query exception
   */
  private int[] bounds(final Long off, final Long len, final int sz) throws QueryException {
    int o = 0;
    final int s;
    if(off != null) {
      if(off < 0 || off > sz || off > Integer.MAX_VALUE) throw BIN_IOOR_X_X.get(info, off, sz);
      o = (int) off.longValue();
    }
    if(len != null) {
      if(len < 0) throw BIN_NS_X.get(info, off);
      if(o + len > sz || len > Integer.MAX_VALUE) throw BIN_IOOR_X_X.get(info, o + len, sz);
      s = (int) len.longValue();
    } else {
      s = sz - o;
    }
    return new int[] { o, s };
  }
}
