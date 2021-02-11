package org.basex.query.value.item;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(AtomType.UNTYPED_ATOMIC);
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
  public byte[] string(final InputInfo ii) {
    return value;
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.length != 0;
  }

  @Override
  public boolean comparable(final Item item) {
    return item.type.isStringOrUntyped();
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.eq(value, item.string(ii)) :
      coll.compare(value, item.string(ii)) == 0 : item.eq(this, coll, sc, ii);
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) throws QueryException {
    return item.type.isStringOrUntyped() && eq(item, null, null, ii);
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    return item.type.isUntyped() ? coll == null ? Token.diff(value, item.string(ii)) :
      coll.compare(value, item.string(ii)) : -item.diff(this, coll, ii);
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
  public void plan(final QueryString qs) {
    qs.quoted(value);
  }
}
