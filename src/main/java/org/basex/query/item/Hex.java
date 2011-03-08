package org.basex.query.item;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * HexBinary item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Bin {
  /**
   * Constructor.
   * @param v value
   */
  public Hex(final byte[] v) {
    super(v, AtomType.HEX);
  }

  /**
   * Constructor.
   * @param v value
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final byte[] v, final InputInfo ii) throws QueryException {
    super(h2b(Token.trim(v), ii), AtomType.HEX);
  }

  /**
   * Constructor.
   * @param b base64 data
   */
  Hex(final B64 b) {
    this(b.val);
  }

  @Override
  public byte[] atom() {
    return Token.hex(val, true);
  }

  /**
   * Converts the hex input into a byte array.
   * @param h hex input
   * @param ii input info
   * @return byte array
   * @throws QueryException query exception
   */
  private static byte[] h2b(final byte[] h, final InputInfo ii)
      throws QueryException {

    if((h.length & 1) != 0) throw FUNCAST.thrw(ii, AtomType.HEX, (char) h[0]);
    final int l = h.length >>> 1;
    final byte[] val = new byte[l];

    for(int i = 0; i < l; ++i) {
      val[i] = (byte) ((h2b(h[i << 1], ii) << 4) + h2b(h[(i << 1) + 1], ii));
    }
    return val;
  }

  /**
   * Converts a single character into a byte value.
   * @param b character
   * @param ii input info
   * @return byte value
   * @throws QueryException query exception
   */
  private static int h2b(final byte b, final InputInfo ii)
      throws QueryException {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'a' && b <= 'f' || b >= 'A' && b <= 'F') return (b & 15) + 9;
    throw FUNCAST.thrw(ii, AtomType.HEX, (char) b);
  }
}
