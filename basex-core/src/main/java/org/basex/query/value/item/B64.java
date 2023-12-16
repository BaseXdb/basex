package org.basex.query.value.item;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * Base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-23, BSD License
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
   * Constructor.
   * @param data binary data
   */
  private B64(final byte[] data) {
    super(data, AtomType.BASE64_BINARY);
  }

  /**
   * Empty constructor.
   */
  B64() {
    this(null);
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
   * @param info input info (can be {@code null})
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final Bin bin, final InputInfo info) throws QueryException {
    return get(bin.binary(info));
  }

  /**
   * Returns an instance of this class.
   * @param value textual representation
   * @param info input info (can be {@code null})
   * @return instance
   * @throws QueryException query exception
   */
  public static B64 get(final byte[] value, final InputInfo info) throws QueryException {
    return get(parse(value, info));
  }

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeToken(binary(null));
  }

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    return Base64.encode(binary(ii));
  }

  @Override
  public final boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final byte[] bin = item instanceof Bin ? ((Bin) item).binary(ii) : parse(item, ii);
    return Token.eq(binary(ii), bin);
  }

  @Override
  public final byte[] parse(final Item item, final InputInfo info) throws QueryException {
    return parse(item.string(info), info);
  }

  /**
   * Converts the given token into a byte array.
   * @param value value to be converted
   * @param info input info (can be {@code null})
   * @return byte array
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] value, final InputInfo info) throws QueryException {
    try {
      return Base64.decode(value);
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw AtomType.BASE64_BINARY.castError(value, info);
    }
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder().add('"');
    if(data.length > 128) {
      tb.add(Base64.encode(Arrays.copyOf(data, 128))).add(Text.DOTS);
    } else {
      tb.add(Base64.encode(data));
    }
    qs.token(tb.add('"').finish());
  }
}
