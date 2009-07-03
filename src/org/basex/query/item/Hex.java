package org.basex.query.item;

import org.basex.BaseX;
import org.basex.query.QueryException;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * HexBinary item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Hex extends Item {
  /** Data. */
  byte[] val;

  /**
   * Constructor.
   * @param b base64 data
   */
  public Hex(final B64 b) {
    super(Type.HEX);
    val = b.val;
  }

  /**
   * Constructor.
   * @param v value
   * @throws QueryException evaluation exception
   */
  public Hex(final byte[] v) throws QueryException {
    super(Type.HEX);
    h2b(Token.trim(v));
  }

  @Override
  public byte[] str() {
    return b2h();
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    if(it.type == type) return Token.eq(val, ((Hex) it).val);
    castErr(it);
    return false;
  }

  @Override
  public Object java() {
    return val;
  }

  /**
   * Converts the specified hex array into a byte array.
   * @param h hex input
   * @throws QueryException evaluation exception
   */
  private void h2b(final byte[] h) throws QueryException {
    if((h.length & 1) != 0) castErr(h);
    final int l = h.length >>> 1;
    val = new byte[l];

    for(int i = 0; i < l; i++) {
      val[i] = (byte) ((h2b(h[i << 1]) << 4) + h2b(h[(i << 1) + 1]));
    }
  }

  /**
   * Converts a single character into a byte value.
   * @param b character
   * @return byte value
   * @throws QueryException evaluation exception
   */
  private int h2b(final byte b) throws QueryException {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'a' && b <= 'f' || b >= 'A' && b <= 'F') return (b & 15) + 9;
    castErr((char) b);
    return 0;
  }

  /**
   * Converts the specified hex array into a byte array.
   * @return hex output
   */
  private byte[] b2h() {
    final TokenBuilder tb = new TokenBuilder();

    for(final byte v : val) {
      tb.add(b2h((v & 0xF0) >> 4));
      tb.add(b2h(v & 0x0F));
    }
    return tb.finish();
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
    return BaseX.info("\"%\"", b2h());
  }
}
