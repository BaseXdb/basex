package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-21, BSD License
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
  public final boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final byte[] str1 = string(ii), str2 = item.string(ii);
    return coll == null ? Token.eq(str1, str2) : coll.compare(str1, str2) == 0;
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, ii);
  }

  @Override
  public final int diff(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    final byte[] str1 = string(ii), str2 = item.string(ii);
    return coll == null ? Token.diff(str1, str2) : coll.compare(str1, str2);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof AStr)) return false;
    final AStr a = (AStr) obj;
    return type == a.type && Token.eq(value, a.value);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.quoted(value);
  }
}
