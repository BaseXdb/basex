package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-17, BSD License
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
    return get(decode(value, ii));
  }

  /**
   * Constructor.
   * @param data binary data
   */
  private B64(final byte[] data) {
    super(data, AtomType.B64);
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return org.basex.util.Base64.encode(binary(ii));
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return Token.eq(binary(ii), it instanceof Bin ? ((Bin) it).binary(ii) :
      decode(it.string(ii), ii));
  }

  @Override
  public final int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return Token.diff(binary(ii), it instanceof Bin ? ((Bin) it).binary(ii) :
      decode(it.string(ii), ii));
  }

  /**
   * Converts the input into a byte array.
   * @param d textual data
   * @param ii input info
   * @return decoded string
   * @throws QueryException query exception
   */
  private static byte[] decode(final byte[] d, final InputInfo ii) throws QueryException {
    try {
      return org.basex.util.Base64.decode(d);
    } catch(final IllegalArgumentException ex) {
      throw castError(AtomType.B64, ex.getMessage().replaceAll("^.*?: |\\.$", ""), ii);
    }
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", org.basex.util.Base64.encode(data));
  }
}
