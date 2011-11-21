package org.basex.query.item;

import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

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
    super(AtomType.JAVA);
    val = v;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Token.token(val.toString());
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return string(ii).length != 0;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(string(ii), it.string(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(string(ii), it.string(ii));
  }

  @Override
  public Object toJava() {
    return val;
  }

  @Override
  public boolean isString() {
    return true;
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", val);
  }
}
