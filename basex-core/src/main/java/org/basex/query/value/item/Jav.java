package org.basex.query.value.item;

import java.util.*;

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
  /** Java object (can be {@code null}). */
  private final Object value;
  /** Query context. */
  private final QueryContext qc;

  /**
   * Constructor.
   * @param value value (can be {@code null})
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
    return value != null && !value.toString().isEmpty();
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
    return obj == this || obj instanceof Jav && Objects.equals(value, ((Jav) obj).value);
  }

  @Override
  public void toString(final QueryString qs) {
    if(value == null) qs.token(value);
    else qs.quoted(Token.token(value.toString()));
  }
}
