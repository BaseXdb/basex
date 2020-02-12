package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /** String data (can be {@code null}). */
  byte[] value;

  /**
   * Constructor.
   */
  AStr() {
    super(AtomType.STR);
  }

  /**
   * Constructor, specifying a type and value.
   * @param type atomic type
   * @param value value
   */
  AStr(final AtomType type, final byte[] value) {
    super(type);
    this.value = value;
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return coll == null ? Token.eq(string(ii), item.string(ii)) :
      coll.compare(string(ii), item.string(ii)) == 0;
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, ii);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return coll == null ? Token.diff(string(ii), item.string(ii)) :
      coll.compare(string(ii), item.string(ii));
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof AStr)) return false;
    final AStr a = (AStr) obj;
    return type == a.type && Token.eq(value, a.value);
  }

  @Override
  public String toString() {
    return Token.string(toQuotedToken(value));
  }
}
