package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.nio.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Functions on binary data.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class BinFn extends StandardFunc {
  /** Big endian order. */
  private static final byte[][] BIG = tokens("most-significant-first", "big-endian", "BE");
  /** Big endian order. */
  private static final byte[][] LITTLE = tokens("least-significant-first", "little-endian", "LE");

  /**
   * Unpacks a double.
   * @param qc query context
   * @param len length of entry
   * @return result
   * @throws QueryException query exception
   */
  final ByteBuffer unpack(final QueryContext qc, final long len) throws QueryException {
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
  final Int unpackInteger(final QueryContext qc, final boolean signed) throws QueryException {
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
  final B64 bit(final Bit op, final QueryContext qc) throws QueryException {
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
   * Pads an array.
   * @param qc query context
   * @param left left/right flag
   * @return result
   * @throws QueryException query exception
   */
  final B64 pad(final QueryContext qc, final boolean left) throws QueryException {
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
   * Checks the octet order.
   * @param o expression offset
   * @param qc query context
   * @return order
   * @throws QueryException query exception
   */
  final ByteOrder order(final int o, final QueryContext qc) throws QueryException {
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
  final int[] bounds(final Long off, final Long len, final int sz) throws QueryException {
    int o = 0;
    if(off != null) {
      if(off < 0 || off > sz || off > Integer.MAX_VALUE) throw BIN_IOOR_X_X.get(info, off, sz);
      o = (int) off.longValue();
    }
    final int s;
    if(len != null) {
      if(len < 0) throw BIN_NS_X.get(info, off);
      if(o + len > sz || len > Integer.MAX_VALUE) throw BIN_IOOR_X_X.get(info, o + len, sz);
      s = (int) len.longValue();
    } else {
      s = sz - o;
    }
    return new int[] { o, s };
  }

  /**
   * Returns a binary string to a byte array.
   * @param bytes binary string
   * @return byte array
   * @throws QueryException query exception
   */
  final byte[] binary2bytes(final byte[] bytes) throws QueryException {
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
   * Converts the specified expression to a string.
   * Returns a token representation or an exception.
   * @param o expression offset
   * @param qc query context
   * @return string representation
   * @throws QueryException query exception
   */
  final byte[] str(final int o, final QueryContext qc) throws QueryException {
    final Item it = exprs[o].atomItem(qc, info);
    return it == null ? null : toToken(it);
  }
}
