package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * HexBinary item ({@code xs:hexBinary}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Bin {
  /**
   * Constructor.
   * @param b bytes
   */
  public Hex(final byte[] b) {
    super(b, AtomType.HEX);
  }

  /**
   * Constructor.
   * @param v textual representation
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final byte[] v, final InputInfo ii) throws QueryException {
    super(decode(Token.trim(v), ii), AtomType.HEX);
  }

  /**
   * Constructor.
   * @param b binary data
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final Bin b, final InputInfo ii) throws QueryException {
    this(b.binary(ii));
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Token.hex(binary(ii), true);
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
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
  public static byte[] decode(final byte[] d, final InputInfo ii) throws QueryException {
    if((d.length & 1) != 0) throw FUNCAST.get(ii, AtomType.HEX, (char) d[0]);
    final int l = d.length >>> 1;
    final byte[] v = new byte[l];
    for(int i = 0; i < l; ++i) {
      v[i] = (byte) ((dec(d[i << 1], ii) << 4) + dec(d[(i << 1) + 1], ii));
    }
    return v;
  }

  /**
   * Converts a single character into a byte value.
   * @param b character
   * @param ii input info
   * @return byte value
   * @throws QueryException query exception
   */
  private static int dec(final byte b, final InputInfo ii) throws QueryException {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'a' && b <= 'f' || b >= 'A' && b <= 'F') return (b & 0x0F) + 9;
    throw FUNCAST.get(ii, AtomType.HEX, (char) b);
  }

  @Override
  public String toString() {
    return new TokenBuilder().add('"').add(Token.hex(data, true)).add('"').toString();
  }
}
