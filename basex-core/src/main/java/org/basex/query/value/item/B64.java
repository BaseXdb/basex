package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class B64 extends Bin {
  /**
   * Empty constructor.
   */
  B64() {
    super(null, AtomType.B64);
  }

  /**
   * Constructor.
   * @param data binary data
   */
  public B64(final byte[] data) {
    super(data, AtomType.B64);
  }

  /**
   * Constructor.
   * @param value textual representation
   * @param ii input info
   * @throws QueryException query exception
   */
  public B64(final byte[] value, final InputInfo ii) throws QueryException {
    super(decode(value, ii), AtomType.B64);
  }

  /**
   * Constructor.
   * @param bin base64 input
   * @param ii input info
   * @throws QueryException query exception
   */
  public B64(final Bin bin, final InputInfo ii) throws QueryException {
    this(bin.binary(ii));
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Base64.encode(binary(ii));
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return Token.eq(binary(ii), it instanceof Bin ? ((Bin) it).binary(ii) :
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
      return Base64.decode(d);
    } catch(final IllegalArgumentException ex) {
      final String chars = ex.getMessage().replaceAll("^.*?: |\\.$", "");
      throw FUNCAST.get(ii, AtomType.B64, chars);
    }
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", Base64.encode(data));
  }
}
