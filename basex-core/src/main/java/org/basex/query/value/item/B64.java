package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class B64 extends Bin {
  /** Empty value. */
  public static final B64 EMPTY = new B64(new byte[0]);
 /** Constant values. */
  private static final B64[] B64S;

  // caches the first 128 integers
  static {
    final int nl = 256;
    B64S = new B64[nl];
    for(int n = 0; n < nl; n++) B64S[n] = new B64(new byte[] { (byte) n });
  }

  /**
   * Empty constructor.
   */
  B64() {
    super(null, AtomType.B64);
  }

  /**
   * Returns an instance of this class for single bytes.
   * @param value value
   * @return instance
   */
  public static B64 get(final byte value) {
    return B64S[value & 0xFF];
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static B64 get(final byte[] value) {
    return value.length == 1 ? get(value[0]) : new B64(value);
  }

  /**
   * Returns an instance of this class.
   * @param bin binary input
   * @param info input info
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final Bin bin, final InputInfo info) throws QueryException {
    return get(bin.binary(info));
  }

  /**
   * Returns an instance of this class.
   * @param value textual representation
   * @param info input info
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final byte[] value, final InputInfo info) throws QueryException {
    return get(parse(value, info));
  }

  /**
   * Constructor.
   * @param data binary data
   */
  private B64(final byte[] data) {
    super(data, AtomType.B64);
  }

  @Override
  public byte[] string(final InputInfo info) throws QueryException {
    return Base64.encode(binary(info));
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(info) : parse(item, info);
    return Token.eq(binary(info), bin);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(info) : parse(item, info);
    return Token.diff(binary(info), bin);
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final Item item, final InputInfo info) throws QueryException {
    try {
      return Base64.decode(item.string(info));
    } catch(final IllegalArgumentException ex) {
      throw AtomType.B64.castError(item, info);
    }
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    try {
      return Base64.decode(value);
    } catch(final IllegalArgumentException ex) {
      throw AtomType.B64.castError(value, info);
    }
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", Base64.encode(data));
  }
}
