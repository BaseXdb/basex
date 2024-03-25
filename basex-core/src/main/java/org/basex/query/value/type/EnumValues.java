package org.basex.query.value.type;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
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
    for(final byte[] val : ev1.values) v.add(val);
    for(final byte[] val : ev2.values) v.add(val);
    return get(v);
  }

  /**
   * Computes the intersection of two instances.
   * @param ev1 first enum values (can be {@code null})
   * @param ev2 second enum values (can be {@code null})
   * @return the intersection, or {@code null} if it is empty
   */
  public static EnumValues intersect(final EnumValues ev1, final EnumValues ev2) {
    if(ev1 == null) return ev2;
    else if(ev2 == null) return ev1;
    final TokenSet v = new TokenSet();
    for(final byte[] val : ev1.values) {
      if(ev2.values.contains(val)) v.add(val);
    }
    return v.isEmpty() ? null : get(v);
  }

  /**
   * Checks whether the given item value is a valid enum value.
   * @param item item
   * @return true, if this instance contains the item value
   * @throws QueryException query exception
   */
  public boolean matches(final Item item) throws QueryException {
    return values.contains(item.string(null));
  }

  /**
   * Checks whether all of the values of this object are also values of the given enum values.
   * @param enumValues enum values to test
   * @return result of check
   */
  public boolean instanceOf(final EnumValues enumValues) {
    for(final byte[] key : values) {
      if(!enumValues.values.contains(key)) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = 31;
    if(values != null) {
      for(final byte[] v : values) {
        result += Token.hash(v);
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof EnumValues)) return false;
    final EnumValues other = (EnumValues) obj;
    if(values == null) {
      if(other.values != null) return false;
    } else {
      if(values.size() != other.values.size()) return false;
      if(!instanceOf(other)) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(AtomType.ENUM).token('(');
    int n = values.size();
    for(final byte[] v : values) {
      qs.quoted(v);
      if(--n != 0) qs.token(',');
    }
    return qs.token(')').toString();
  }
}
