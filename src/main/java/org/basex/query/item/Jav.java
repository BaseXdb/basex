package org.basex.query.item;

import org.basex.query.*;
import org.basex.util.*;

/**
 * String item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  private final Object val;

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
  public String toString() {
    return Util.info("\"%\"", val);
  }
}
