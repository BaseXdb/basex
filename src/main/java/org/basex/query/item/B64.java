package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryException;
import org.basex.util.Base64;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Base64Binary item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class B64 extends Bin {
  /**
   * Constructor.
   * @param d binary data
   */
  public B64(final byte[] d) {
    super(d, AtomType.B64);
  }

  /**
   * Constructor.
   * @param v textual representation
   * @param ii input info
   * @throws QueryException query exception
   */
  public B64(final byte[] v, final InputInfo ii) throws QueryException {
    super(decode(v, ii), AtomType.B64);
  }

  /**
   * Constructor.
   * @param b binary item
   * @param ii input info
   * @throws QueryException query exception
   */
  B64(final Bin b, final InputInfo ii) throws QueryException {
    this(b.val(ii));
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Base64.encode(val);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    return Token.eq(val(ii), it instanceof Bin ? ((Bin) it).val(ii) :
      decode(it.string(ii), ii));
  }

  /**
   * Converts the input into a byte array.
   * @param d textual data
   * @param ii input info
   * @return decoded string
   * @throws QueryException query exception
   */
  private static byte[] decode(final byte[] d, final InputInfo ii)
      throws QueryException {
    try {
      return Base64.decode(d);
    } catch(final IllegalArgumentException ex) {
      final String chars = ex.getMessage().replaceAll(".*?: |\\.$", "");
      throw FUNCAST.thrw(ii, AtomType.B64, chars);
    }
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", string(null));
  }
}
