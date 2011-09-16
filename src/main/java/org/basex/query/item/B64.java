package org.basex.query.item;

import static org.basex.query.util.Err.*;

import java.io.InputStream;

import org.basex.io.in.ArrayInput;
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
   * @param b binary item
   * @param ii input info
   * @throws QueryException query exception
   */
  B64(final Bin b, final InputInfo ii) throws QueryException {
    this(b.val(ii));
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    // at this stage, item will always be of the same type
    return Token.eq(val, it instanceof Bin ? ((Bin) it).val(ii) :
      decode(it.atom(ii), ii));
  }

  @Override
  public byte[] atom(final InputInfo ii) {
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

  @Override
  protected byte[] val(InputInfo ii) {
    return val;
  }

  @Override
  public InputStream input() {
    return new ArrayInput(val);
  }

  @Override
  public final String toString() {
    return Util.info("\"%\"", atom(null));
  }
}
