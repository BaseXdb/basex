package org.basex.query.value.item;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * Base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(null, AtomType.BASE64_BINARY);
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
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final Bin bin, final InputInfo ii) throws QueryException {
    return get(bin.binary(ii));
  }

  /**
   * Returns an instance of this class.
   * @param value textual representation
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final byte[] value, final InputInfo ii) throws QueryException {
    return get(parse(value, ii));
  }

  /**
   * Constructor.
   * @param data binary data
   */
  private B64(final byte[] data) {
    super(data, AtomType.BASE64_BINARY);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Base64.encode(binary(ii));
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.eq(binary(ii), bin);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.diff(binary(ii), bin);
  }

  /**
   * Converts the given item to a byte array.
   * @param item item to be converted
   * @param ii input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final Item item, final InputInfo ii) throws QueryException {
    try {
      return Base64.decode(item.string(ii));
    } catch(final IllegalArgumentException ex) {
      throw AtomType.BASE64_BINARY.castError(item, ii);
    }
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param ii input info
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo ii) throws QueryException {
    try {
      return Base64.decode(value);
    } catch(final IllegalArgumentException ex) {
      throw AtomType.BASE64_BINARY.castError(value, ii);
    }
  }

  @Override
  public void plan(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('"');
    if(data.length > 128) {
      tb.add(Base64.encode(Arrays.copyOf(data, 128))).add(Text.DOTS);
    } else {
      tb.add(Base64.encode(data));
    }
    qs.token(tb.add('"').finish());
  }
}
