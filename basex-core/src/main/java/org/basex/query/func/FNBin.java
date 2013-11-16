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
import org.basex.query.expr.*;
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
 * @author BaseX Team 2005-13, BSD License
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
    /** And. */ AND;
  }
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNBin(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _BIN_TO_OCTETS: return toOctetsIter(ctx);
      default:             return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _BIN_TO_OCTETS: return toOctets(ctx);
      default:             return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _BIN_HEX:                     return hex(ctx);
      case _BIN_BIN:                     return bin(ctx);
      case _BIN_OCTAL:                   return octal(ctx);
      case _BIN_FROM_OCTETS:             return fromOctets(ctx);
      case _BIN_LENGTH:                  return length(ctx);
      case _BIN_PART:                    return part(ctx);
      case _BIN_JOIN:                    return join(ctx);
      case _BIN_INSERT_BEFORE:           return insertBefore(ctx);
      case _BIN_PAD_LEFT:                return pad(ctx, true);
      case _BIN_PAD_RIGHT:               return pad(ctx, false);
      case _BIN_FIND:                    return find(ctx);
      case _BIN_DECODE_STRING:           return decodeString(ctx);
      case _BIN_ENCODE_STRING:           return encodeString(ctx);
      case _BIN_PACK_DOUBLE:             return packDouble(ctx);
      case _BIN_PACK_FLOAT:              return packFloat(ctx);
      case _BIN_PACK_INTEGER:            return packInteger(ctx);
      case _BIN_UNPACK_DOUBLE:           return Dbl.get(unpack(ctx, 8).getDouble());
      case _BIN_UNPACK_FLOAT:            return Flt.get(unpack(ctx, 4).getFloat());
      case _BIN_UNPACK_INTEGER:          return unpackInteger(ctx, true);
      case _BIN_UNPACK_UNSIGNED_INTEGER: return unpackInteger(ctx, false);
      case _BIN_OR:                      return bit(Bit.OR, ctx);
      case _BIN_XOR:                     return bit(Bit.XOR, ctx);
      case _BIN_AND:                     return bit(Bit.AND, ctx);
      case _BIN_NOT:                     return not(ctx);
      case _BIN_SHIFT:                   return shift(ctx);
      default:                           return super.item(ctx, ii);
    }
  }

  /**
   * Converts a hexadecimal string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 hex(final QueryContext ctx) throws QueryException {
    byte[] bytes = str(0, ctx);
    if(bytes == null) return null;

    // add leading zero
    if((bytes.length & 1) != 0) bytes = concat(ZERO, bytes);
    try {
      return new B64(Hex.decode(bytes, info));
    } catch(final QueryException ex) {
      throw BIN_NNC.thrw(info);
    }
  }

  /**
   * Converts a binary string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 bin(final QueryContext ctx) throws QueryException {
    final byte[] bytes = str(0, ctx);
    if(bytes == null) return null;
    return new B64(binary2bytes(bytes));
  }

  /**
   * Converts an octal string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 octal(final QueryContext ctx) throws QueryException {
    final byte[] bytes = str(0, ctx);
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
      throw BIN_NNC.thrw(info);
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
        throw BIN_NNC.thrw(info);
      }
    }
    return tmp;
  }

  /**
   * Converts binary data to integers.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Value toOctets(final QueryContext ctx) throws QueryException {
    final B64 b = b64(expr[0], false, ctx);
    final byte[] bytes = b.binary(info);
    final long[] vals = new long[bytes.length];
    for(int i = 0; i < bytes.length; i++) vals[i] = bytes[i] & 0xFF;
    return IntSeq.get(vals, AtomType.ITR);
  }

  /**
   * Converts binary data to integers.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter toOctetsIter(final QueryContext ctx) throws QueryException {
    final B64 b = b64(expr[0], false, ctx);
    final byte[] bytes = b.binary(info);
    return new ValueIter() {
      int c;
      @Override
      public Int get(final long i) { return Int.get(bytes[(int) i] & 0xFF); }
      @Override
      public Int next() { return c < size ? get(c++) : null; }
      @Override
      public boolean reset() { c = 0; return true; }
      @Override
      public long size() { return size; }
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
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 fromOctets(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr[0]);
    final ByteList bl = new ByteList(Math.max(Array.CAPACITY, (int) ir.size()));
    for(Item it; (it = ir.next()) != null;) {
      final long l = checkItr(it);
      if(l < 0 || l > 255) throw BIN_OOR_X.thrw(info, l);
      bl.add((int) l);
    }
    return new B64(bl.toArray());
  }

  /**
   * Returns the length of binary array.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Int length(final QueryContext ctx) throws QueryException {
    final B64 b = b64(expr[0], false, ctx);
    return Int.get(b.binary(info).length);
  }

  /**
   * Returns part of a binary array.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 part(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    final Long off = checkItr(expr[1], ctx);
    final Long len = expr.length > 2 ? checkItr(expr[2], ctx) : null;
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
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 join(final QueryContext ctx) throws QueryException {
    final ByteList bl = new ByteList();
    final Iter ir = ctx.iter(expr[0]);
    for(Item it; (it = ir.next()) != null;) bl.add(checkBinary(it, ctx).binary(info));
    return new B64(bl.toArray());
  }

  /**
   * Inserts data into binary data.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 insertBefore(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    final Long off = checkItr(expr[1], ctx);
    final B64 xtr = b64(expr[2], true, ctx);
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
   * @param ctx query context
   * @param left left/right flag
   * @return result
   * @throws QueryException query exception
   */
  private B64 pad(final QueryContext ctx, final boolean left) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    final long sz = checkItr(expr[1], ctx);
    final long octet = expr.length > 2 ? checkItr(expr[2], ctx) : 0;
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    if(sz < 0) throw BIN_NS_X.thrw(info, sz);
    if(octet < 0 || octet > 255) throw BIN_OOR_X.thrw(info, octet);

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
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Int find(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    final Long off = checkItr(expr[1], ctx);
    final B64 srch = b64(expr[2], false, ctx);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final byte[] search = srch.binary(info);
    final int sl = search.length;

    final int[] bounds = bounds(off, null, bl);
    if(sl == 0) throw BIN_ESI.thrw(info, off);

    final int pos = indexOf(bytes, search, bounds[0]);
    return pos == -1 ? null : Int.get(pos);
  }

  /**
   * Converts binary data to a string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str decodeString(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    final String enc = encoding(1, BIN_UE_X, ctx);
    final Long off = expr.length > 2 ? checkItr(expr[2], ctx) : null;
    final Long len = expr.length > 3 ? checkItr(expr[3], ctx) : null;
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
      throw BIN_DE.thrw(info, ex);
    }
  }

  /**
   * Converts a string to binary data.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 encodeString(final QueryContext ctx) throws QueryException {
    final byte[] str = str(0, ctx);
    final String enc = encoding(1, BIN_UE_X, ctx);
    if(str == null) return null;
    try {
      return new B64(FNConvert.toBinary(str, enc));
    } catch(final CharacterCodingException ex) {
      throw BIN_EE.thrw(info);
    }
  }

  /**
   * Packs a double.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packDouble(final QueryContext ctx) throws QueryException {
    final double d = checkDbl(expr[0], ctx);
    final ByteOrder bo = order(1, ctx);
    return new B64(ByteBuffer.wrap(new byte[8]).order(bo).putDouble(d).array());
  }

  /**
   * Packs a float.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packFloat(final QueryContext ctx) throws QueryException {
    final float f = checkFlt(expr[0], ctx);
    final ByteOrder bo = order(1, ctx);
    return new B64(ByteBuffer.wrap(new byte[4]).order(bo).putFloat(f).array());
  }

  /**
   * Packs an integer.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 packInteger(final QueryContext ctx) throws QueryException {
    long b = checkItr(expr[0], ctx);
    final long sz = checkItr(expr[1], ctx);
    final ByteOrder bo = order(2, ctx);

    if(sz < 0 || sz > Long.MAX_VALUE) throw BIN_NS_X.thrw(info, sz);

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
   * @param ctx query context
   * @param len length of entry
   * @return result
   * @throws QueryException query exception
   */
  private ByteBuffer unpack(final QueryContext ctx, final long len) throws QueryException {
    final B64 b64 = b64(expr[0], false, ctx);
    final Long off = checkItr(expr[1], ctx);
    final ByteOrder bo = order(2, ctx);

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;
    final int[] bounds = bounds(off, len, bl);

    final ByteBuffer bb = ByteBuffer.allocate(bounds[1]).order(bo);
    bb.put(bytes, bounds[0], bounds[1]).position(0);
    return bb;
  }

  /**
   * Unpacks an unsigned integer.
   * @param ctx query context
   * @param signed signed flag
   * @return result
   * @throws QueryException query exception
   */
  private Int unpackInteger(final QueryContext ctx, final boolean signed)
      throws QueryException {

    final B64 b64 = b64(expr[0], false, ctx);
    final Long off = checkItr(expr[1], ctx);
    final Long sz = checkItr(expr[2], ctx);
    final ByteOrder bo = order(3, ctx);

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
      final int s = l;
      System.arraycopy(bytes, o, tmp, 0, l);
      if(neg) for(int i = s; i < 8; i++) tmp[i] = (byte) 0xFF;
    }
    return Int.get(ByteBuffer.wrap(tmp).order(bo).getLong());
  }

  /**
   * Returns bitwise operations.
   * @param op bit operation
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 bit(final Bit op, final QueryContext ctx) throws QueryException {
    final B64 b1 = b64(expr[0], true, ctx);
    final B64 b2 = b64(expr[1], true, ctx);
    if(b1 == null || b2 == null) return null;

    final byte[] bytes1 = b1.binary(info);
    final byte[] bytes2 = b2.binary(info);
    final int bl1 = bytes1.length;
    final int bl2 = bytes2.length;
    if(bl1 != bl2) throw BIN_DLA_X_X.thrw(info, bl1, bl2);

    final byte[] tmp = new byte[bl1];
    for(int b = 0; b < bl1; b++) {
      tmp[b] = (byte) (op == Bit.OR ? bytes1[b] | bytes2[b] :
        op == Bit.XOR ? bytes1[b] ^ bytes2[b] : bytes1[b] & bytes2[b]);
    }
    return new B64(tmp);
  }

  /**
   * Returns the bitwise not operation.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 not(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    final byte[] tmp = new byte[bl];
    for(int b = 0; b < bl; b++) tmp[b] = (byte) ~bytes[b];
    return new B64(tmp);
  }

  /**
   * Returns the bitwise shift operation.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private B64 shift(final QueryContext ctx) throws QueryException {
    final B64 b64 = b64(expr[0], true, ctx);
    long by = checkItr(expr[1], ctx);
    if(b64 == null) return null;

    final byte[] bytes = b64.binary(info);
    final int bl = bytes.length;

    final byte[] tmp = new byte[bl];
    int r = 0;
    if(by > 0) {
      for(int i = bl - 1; i >= 0; i--) {
        final byte b = bytes[i];
        tmp[i] = (byte) (b << by | r);
        r = b >>> 32 - by;
      }
    } else {
      by = -by;
      for(int i = 0; i < bl; i++) {
        final byte b = bytes[i];
        tmp[i] = (byte) (b >>> by | r);
        r = b << 32 - by;
      }
    }
    return new B64(tmp);
  }

  // UTILITY FUNCTIONS ============================================================================

  /**
   * Checks if the specified expression yields a binary item.
   * @param e expression to be evaluated
   * @param empty allow empty sequence
   * @param ctx query context
   * @return binary item
   * @throws QueryException query exception
   */
  private B64 b64(final Expr e, final boolean empty, final QueryContext ctx) throws QueryException {
    final Item it = e.item(ctx, info);
    if(it == null) {
      if(empty) return null;
      throw INVEMPTY.thrw(info, description());
    }
    return (B64) checkType(it, AtomType.B64);
  }

  /**
   * Converts the specified expression to a string.
   * Returns a token representation or an exception.
   * @param o expression offset
   * @param ctx query context
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] str(final int o, final QueryContext ctx) throws QueryException {
    final Item it = expr[o].item(ctx, info);
    return it == null ? null : checkStr(it);
  }

  /**
   * Checks the octet order.
   * @param o expression offset
   * @param ctx query context
   * @return order
   * @throws QueryException query exception
   */
  private ByteOrder order(final int o, final QueryContext ctx) throws QueryException {
    if(o >= expr.length) return ByteOrder.BIG_ENDIAN;
    final byte[] order = checkStr(expr[o], ctx);
    if(eq(order, BIG)) return ByteOrder.BIG_ENDIAN;
    if(eq(order, LITTLE)) return ByteOrder.LITTLE_ENDIAN;
    throw BIN_USO_X.thrw(info, order);
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
    int o = 0, s = sz;
    if(off != null) {
      if(off < 0) throw BIN_NO_X.thrw(info, off);
      if(off > sz || off > Integer.MAX_VALUE) throw BIN_OBE_X_X.thrw(info, off);
      o = (int) off.longValue();
    }
    if(len != null) {
      if(len < 0) throw BIN_NS_X.thrw(info, off);
      if(o + len > sz || len > Integer.MAX_VALUE) throw BIN_OBE_X_X_X.thrw(info, off, len, sz);
      s = (int) len.longValue();
    } else {
      s = sz - o;
    }
    return new int[] { o, s };
  }
}
