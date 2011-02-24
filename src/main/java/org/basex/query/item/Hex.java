package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.util.ByteList;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * HexBinary item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Hex extends Item {
  /** Data. */
  byte[] val;

  /**
   * Constructor.
   * @param v value
   */
  public Hex(final byte[] v) {
    super(AtomType.HEX);
    val = v;
  }

  /**
   * Constructor.
   * @param v value
   * @param ii input info
   * @throws QueryException query exception
   */
  public Hex(final byte[] v, final InputInfo ii) throws QueryException {
    super(AtomType.HEX);
    h2b(Token.trim(v), ii);
  }

  /**
   * Constructor.
   * @param b base64 data
   */
  Hex(final B64 b) {
    this(b.val);
  }

  @Override
  public byte[] atom(final InputInfo ii) {
    return b2h();
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) {
    // at this stage, item will always be of the same type
    return Token.eq(val, ((Hex) it).val);
  }

  @Override
  public byte[] toJava() {
    return val;
  }

  /**
   * Converts the specified hex array into a byte array.
   * @param h hex input
   * @param ii input info
   * @throws QueryException query exception
   */
  private void h2b(final byte[] h, final InputInfo ii) throws QueryException {
    if((h.length & 1) != 0) castErr(h, ii);
    final int l = h.length >>> 1;
    val = new byte[l];

    for(int i = 0; i < l; ++i) {
      val[i] = (byte) ((h2b(h[i << 1], ii) << 4) + h2b(h[(i << 1) + 1], ii));
    }
  }

  /**
   * Converts a single character into a byte value.
   * @param b character
   * @param ii input info
   * @return byte value
   * @throws QueryException query exception
   */
  private int h2b(final byte b, final InputInfo ii) throws QueryException {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'a' && b <= 'f' || b >= 'A' && b <= 'F') return (b & 15) + 9;
    castErr((char) b, ii);
    return 0;
  }

  /**
   * Converts the specified hex array into a byte array.
   * @return hex output
   */
  private byte[] b2h() {
    final ByteList bl = new ByteList();
    for(final byte v : val) {
      bl.add(b2h((v & 0xF0) >> 4));
      bl.add(b2h(v & 0x0F));
    }
    return bl.toArray();
  }

  /**
   * Converts a byte value into a hex character.
   * @param b byte value
   * @return hex character
   */
  private byte b2h(final int b) {
    return (byte) (b + (b < 0x0A ? '0' : '7'));
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", b2h());
  }
}
