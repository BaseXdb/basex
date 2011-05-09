package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryException;
import org.basex.util.Base64;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Base64Binary item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class B64 extends Bin {
  /**
   * Constructor.
   * @param d textual data
   * @param ii input info
   * @throws QueryException query exception
   */
  public B64(final byte[] d, final InputInfo ii) throws QueryException {
    super(decode(d, ii), AtomType.B64);
  }

  /**
   * Constructor.
   * @param d binary data
   */
  public B64(final byte[] d) {
    super(d, AtomType.B64);
  }

  /**
   * Constructor.
   * @param h hex item
   */
  B64(final Hex h) {
    this(h.val);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    // at this stage, item will always be of the same type
    return Token.eq(val, it instanceof Bin ? ((Bin) it).val :
      decode(it.atom(ii), ii));
  }

  @Override
  public byte[] atom() {
    return Base64.encode(val);
  }

  /**
   * Constructor.
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
}
