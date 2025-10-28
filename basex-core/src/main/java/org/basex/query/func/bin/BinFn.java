package org.basex.query.func.bin;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.math.*;
import java.nio.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on binary data.
 *
 * @author BaseX Team, BSD License
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
    final Bin binary = toBin(arg(0), qc);
    final long offset = toLong(arg(1), qc);
    final ByteOrder order = order(arg(2), qc);

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(offset, len, bl);

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
  final Itr unpackInteger(final QueryContext qc, final boolean signed) throws QueryException {
    final Bin binary = toBin(arg(0), qc);
    final long offset = toLong(arg(1), qc);
    final long size = toLong(arg(2), qc);
    final ByteOrder order = order(arg(3), qc);

    final byte[] bytes = binary.binary(info);
    final int[] bounds = bounds(offset, size, bytes.length);
    final int o = bounds[0], l = bounds[1];
    if(l == 0) return Itr.ZERO;

    final byte[] result = new byte[l];
    ByteBuffer.wrap(bytes, o, l).order(order).get(result);

    if(order == ByteOrder.LITTLE_ENDIAN) {
      for(int i = 0, j = l - 1; i < j; i++, j--) {
        final byte tmp = result[i];
        result[i] = result[j];
        result[j] = tmp;
      }
    }
    final BigInteger bi = signed ? new BigInteger(result) : new BigInteger(1, result);
    final long v = bi.longValue();
    if(BigInteger.valueOf(v).equals(bi)) return Itr.get(v);
    throw BIN_ITL_X_X.get(info, bi);
  }

  /**
   * Returns bitwise operations.
   * @param op bit operation
   * @param qc query context
   * @return result or {@code null}
   * @throws QueryException query exception
   */
  final Item bit(final Bit op, final QueryContext qc) throws QueryException {
    final Bin binary1 = toBinOrNull(arg(0), qc), binary2 = toBinOrNull(arg(1), qc);
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
    final Bin binary = toBinOrNull(arg(0), qc);
    final long size = toLong(arg(1), qc);
    final Long octet = toLongOrNull(arg(2), qc);
    if(binary == null) return Empty.VALUE;

    final byte[] bytes = binary.binary(info);
    final int bl = bytes.length;

    if(size < 0) throw BIN_NS_X.get(info, size);
    final long oct = octet != null ? octet : 0;
    if(oct < 0 || oct > 255) throw BIN_OOR_X.get(info, octet);

    final byte[] tmp = new byte[(int) (bl + size)];
    if(left) {
      Arrays.fill(tmp, 0, (int) size, (byte) oct);
      Array.copyFromStart(bytes, bl, tmp, (int) size);
    } else {
      Array.copy(bytes, bl, tmp);
      Arrays.fill(tmp, bl, tmp.length, (byte) oct);
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
    final byte[] order = toTokenOrNull(expr, qc);
    if(order == null || eq(order, BIG)) return ByteOrder.BIG_ENDIAN;
    if(eq(order, LITTLE)) return ByteOrder.LITTLE_ENDIAN;
    throw BIN_USO_X.get(info, order);
  }

  /**
   * Checks the bounds of the specified offset value.
   * @param offset offset value (can be empty sequence)
   * @param length length value (can be empty sequence)
   * @param size size of input data
   * @return bounds (two integers)
   * @throws QueryException query exception
   */
  final int[] bounds(final Long offset, final Long length, final int size) throws QueryException {
    int of = 0;
    if(offset != null) {
      if(offset < 0 || offset > size) throw BIN_IOOR_X_X.get(info, offset, size);
      of = (int) offset.longValue();
    }
    final int sz;
    if(length != null) {
      if(length < 0) throw BIN_NS_X.get(info, offset);
      if(of + length > size || length > Integer.MAX_VALUE)
        throw BIN_IOOR_X_X.get(info, of + length, size);
      sz = (int) length.longValue();
    } else {
      sz = size - of;
    }
    return new int[] { of, sz };
  }


  /**
   * Returns digits in a string to be converted to a byte array.
   * @param qc query context
   * @return normalized digits or {@code null}
   * @throws QueryException query exception
   */
  final byte[] toDigits(final QueryContext qc) throws QueryException {
    final byte[] string = toTokenOrNull(arg(0), qc);
    if(string == null) return null;
    final ByteList bl = new ByteList(string.length);
    for(final byte b : string) {
      if(!(b == '_' || ws(b))) bl.add(b);
    }
    return bl.finish();
  }

  /**
   * Converts a binary string to a byte array.
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
