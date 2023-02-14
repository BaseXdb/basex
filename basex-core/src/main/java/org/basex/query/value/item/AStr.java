package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /** String data (can be {@code null}). */
  byte[] value;

  /**
   * Constructor.
   */
  AStr() {
    super(AtomType.STRING);
  }

  /**
   * Constructor, specifying a type and value.
   * @param value value
   * @param type atomic type
   */
  AStr(final byte[] value, final Type type) {
    super(type);
    this.value = value;
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return Token.eq(string(ii), item.string(ii), coll);
  }

  @Override
  public boolean atomicEq(final Item item, final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, ii);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return Token.diff(string(ii), item.string(ii), coll);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof AStr)) return false;
    final AStr a = (AStr) obj;
    return type == a.type && Token.eq(value, a.value);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.quoted(value);
  }
}
