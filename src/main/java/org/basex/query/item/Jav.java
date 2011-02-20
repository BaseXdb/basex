package org.basex.query.item;

import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  public final Object val;

  /**
   * Constructor.
   * @param v value
   */
  public Jav(final Object v) {
    super(SimpleType.JAVA);
    val = v;
  }

  @Override
  public byte[] atom() {
    return Token.token(val.toString());
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return atom().length != 0;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) {
    return Token.eq(atom(), it.atom());
  }

  @Override
  public int diff(final InputInfo ii, final Item it) {
    return Token.diff(atom(), it.atom());
  }

  @Override
  public Object toJava() {
    return val;
  }

  @Override
  public String toString() {
    return "\"" + val + "\"";
  }
}
