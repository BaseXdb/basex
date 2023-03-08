package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.nio.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Functions on binary data.
 *
 * @author BaseX Team 2005-23, BSD License
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
    final B64 binary = toB64(arg(0), qc, false);
    final Item offset = arg(1).atomItem(qc, info);
    final ByteOrder order = order(arg(2), qc);

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, Int.get(len), bl);

    final ByteBuffer bb = ByteBuffer.allocate(bounds[1]).order(order);
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
    final B64 binary = toB64(arg(0), qc, false);
    final Item offset = arg(1).atomItem(qc, info);
    final Item size = arg(2).atomItem(qc, info);
    final ByteOrder order = order(arg(3), qc);

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, size, bl);
    final int o = bounds[0], l = Math.min(8, bounds[1]);
    if(l == 0) return Int.ZERO;

    // place input data in long byte array, consider sign
    final byte[] tmp = new byte[8];
    final boolean neg = signed && (bytes[0] & 0x80) != 0;
    if(order == ByteOrder.BIG_ENDIAN) {
      final int s = 8 - l;
      if(neg) for(int i = 0; i < s; i++) tmp[i] = (byte) 0xFF;
      Array.copy(bytes, o, l, tmp, s);
    } else {
      Array.copyToStart(bytes, o, l, tmp);
      if(neg) for(int i = l; i < 8; i++) tmp[i] = (byte) 0xFF;
    }
    return Int.get(ByteBuffer.wrap(tmp).order(order).getLong());
  }

  /**
   * Returns bitwise operations.
   * @param op bit operation
   * @param qc query context
   * @return result or {@code null}
   * @throws QueryException query exception
   */
  final Item bit(final Bit op, final QueryContext qc) throws QueryException {
    final B64 binary1 = toB64(arg(0), qc, true), binary2 = toB64(arg(1), qc, true);
    if(binary1 == null || binary2 == null) return Empty.VALUE;

    final byte[] bytes1 = binary1.binary(info), bytes2 = binary2.binary(info);
    final int bl1 = bytes1.length, bl2 = bytes2.length;
    if(bl1 != bl2) throw BIN_DLA_X_X.get(info, bl1, bl2);

    // single byte
    if(bl1 == 1) return B64.get(op.eval(bytes1[0], bytes2[0]));
    // byte array
    final byte[] tmp = new byte[bl1];
    for(int b = 0; b < bl1; b++) tmp[b] = op.eval(bytes1[b], bytes2[b]);
    return B64.get(tmp);
  }

  /**
   * Pads an array.
   * @param qc query context
   * @param left left/right flag
   * @return result or {@code null}
   * @throws QueryException query exception
   */
  final Item pad(final QueryContext qc, final boolean left) throws QueryException {
    final B64 binary = toB64(arg(0), qc, true);
    final long size = toLong(arg(1), qc);
    final long octet = defined(2) ? toLong(arg(2), qc) : 0;
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;

    if(size < 0) throw BIN_NS_X.get(info, size);
    if(octet < 0 || octet > 255) throw BIN_OOR_X.get(info, octet);

    final byte[] tmp = new byte[(int) (bl + size)];
    if(left) {
      Arrays.fill(tmp, 0, (int) size, (byte) octet);
      Array.copyFromStart(bytes, bl, tmp, (int) size);
    } else {
      Array.copy(bytes, bl, tmp);
      Arrays.fill(tmp, bl, tmp.length, (byte) octet);
    }
    return B64.get(tmp);
  }

  /**
   * Checks the octet order.
   * @param expr expression (can be {@code Empty#UNDEFINED})
   * @param qc query context
   * @return order
   * @throws QueryException query exception
   */
  final ByteOrder order(final Expr expr, final QueryContext qc) throws QueryException {
    if(expr == Empty.UNDEFINED) return ByteOrder.BIG_ENDIAN;
    final byte[] order = toToken(expr, qc);
    if(eq(order, BIG)) return ByteOrder.BIG_ENDIAN;
    if(eq(order, LITTLE)) return ByteOrder.LITTLE_ENDIAN;
    throw BIN_USO_X.get(info, order);
  }

  /**
   * Checks the bounds of the specified offset value.
   * @param offset offset value (may be empty sequence)
   * @param length length value (may be empty sequence)
   * @param size size of input data
   * @return bounds (two integers)
   * @throws QueryException query exception
   */
  final int[] bounds(final Item offset, final Item length, final int size) throws QueryException {
    final Long off = offset.isEmpty() ? null : toLong(offset);
    final Long len = length.isEmpty() ? null : toLong(length);

    int of = 0;
    if(off != null) {
      if(off < 0 || off > size) throw BIN_IOOR_X_X.get(info, off, size);
      of = (int) off.longValue();
    }
    final int sz;
    if(len != null) {
      if(len < 0) throw BIN_NS_X.get(info, off);
      if(of + len > size || len > Integer.MAX_VALUE) throw BIN_IOOR_X_X.get(info, of + len, size);
      sz = (int) len.longValue();
    } else {
      sz = size - of;
    }
    return new int[] { of, sz };
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
}
