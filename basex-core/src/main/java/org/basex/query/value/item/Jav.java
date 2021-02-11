package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Java item.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  private final Object value;
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param value value
   * @param qc query context
   */
  public Jav(final Object value, final QueryContext qc) {
    super(AtomType.JAVA);
    this.value = value;
    this.qc = qc;
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    return Str.get(value, qc, ii).value;
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return !value.toString().isEmpty();
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return Token.eq(string(ii), item.string(ii));
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) {
    return false;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    return Token.diff(string(ii), item.string(ii));
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof Jav && value.equals(((Jav) obj).value);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.quoted(Token.token(value.toString()));
  }
}
