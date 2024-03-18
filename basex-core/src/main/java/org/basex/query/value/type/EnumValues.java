package org.basex.query.value.type;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * Values of an enum type.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public final class EnumValues {
  /** The enum values. */
  private final TokenSet values;

  /**
   * Constructor.
   * @param values enum values
   */
  private EnumValues(final TokenSet values) {
    this.values = values;
  }

  /**
   * Creates an instance from enum values in a token set.
   * @param ts enum values
   * @return the new instance
   */
  public static EnumValues get(final TokenSet ts) {
    return new EnumValues(ts);
  }

  /**
   * Combine two instances into one.
   * @param ev1 first enum values (can be {@code null})
   * @param ev2 second enum values (can be {@code null})
   * @return combined enum values
   */
  public static EnumValues get(final EnumValues ev1, final EnumValues ev2) {
    if(ev1 == null || ev2 == null) return null;
    final TokenSet v = new TokenSet();
    ev1.values.forEach(val -> v.add(val));
    ev2.values.forEach(val -> v.add(val));
    return get(v);
  }

  /**
   * Computes the intersection of two instances.
   * @param ev1 first enum values (can be {@code null})
   * @param ev2 second enum values (can be {@code null})
   * @return the intersection.
   */
  public static EnumValues intersect(final EnumValues ev1, final EnumValues ev2) {
    final TokenSet v = new TokenSet();
    if(ev1 != null && ev2 != null) {
      ev1.values.forEach(val -> {
        if(ev2.values.contains(val)) v.add(val);
      });
    }
    return get(v);
  }

  /**
   * Checks whether the given item value is valid.
   * @param item item
   * @return true, if this instance contains the item value
   * @throws QueryException query exception
   */
  public boolean matches(final Item item) throws QueryException {
    return values.contains(item.string(null));
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(AtomType.ENUM).token('(');
    int n = values.size();
    for(byte[] v : values) {
      qs.quoted(v);
      if(--n != 0) qs.token(',');
    }
    return qs.token(')').toString();
  }
}
