package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Java item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  private final Object val;
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param o value
   * @param ctx query context
   */
  public Jav(final Object o, final QueryContext ctx) {
    super(AtomType.JAVA);
    val = o;
    qc = ctx;
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Str.get(val, qc, ii).val;
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !val.toString().isEmpty();
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
