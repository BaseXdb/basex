package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class Atm extends Item {
  /** String data. */
  private final byte[] value;

  /**
   * Constructor.
   * @param value value
   */
  public Atm(final byte[] value) {
    super(AtomType.ATM);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value value
   */
  public Atm(final String value) {
    this(Token.token(value));
  }

  @Override
  public byte[] string(final InputInfo info) {
    return value;
  }

  @Override
  public boolean bool(final InputInfo info) {
    return value.length != 0;
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.eq(value, item.string(info)) :
      coll.compare(value, item.string(info)) == 0 : item.eq(this, coll, sc, info);
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo info) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, info);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.diff(value, item.string(info)) :
      coll.compare(value, item.string(info)) : -item.diff(this, coll, info);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Atm && Token.eq(value, ((Atm) obj).value);
  }

  @Override
  public String toJava() {
    return Token.string(value);
  }

  @Override
  public String toString() {
    return toString(value);
  }
}
