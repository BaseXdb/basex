package org.basex.query.value.type;

import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Values of an enum type.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public final class EnumValues {
  /** The enumeration values (at least one). */
  private final TokenSet values;

  /**
   * Constructor.
   * @param values enumeration values (at least one)
   */
  public EnumValues(final TokenSet values) {
    this.values = values;
  }

  /**
   * Combine the union of two instances.
   * @param ev1 first enum values (can be {@code null})
   * @param ev2 second enum values (can be {@code null})
   * @return combined enum values
   */
  public static EnumValues union(final EnumValues ev1, final EnumValues ev2) {
    if(ev1 == null || ev2 == null) return null;
    final TokenSet ts = new TokenSet(), values1 = ev1.values, values2 = ev2.values;
    for(final byte[] value : values1) ts.add(value);
    for(final byte[] value : values2) ts.add(value);
    return ts.size() == values1.size() ? ev1 : new EnumValues(ts);
  }

  /**
   * Computes the intersection of two instances.
   * @param ev1 first enum values (can be {@code null})
   * @param ev2 second enum values (can be {@code null})
   * @return the intersection, or {@code null} if it is empty
   */
  public static EnumValues intersect(final EnumValues ev1, final EnumValues ev2) {
    if(ev1 == null) return ev2;
    if(ev2 == null) return ev1;
    final TokenSet ts = new TokenSet(), values1 = ev1.values, values2 = ev2.values;
    for(final byte[] value : values1) {
      if(values2.contains(value)) ts.add(value);
    }
    return ts.isEmpty() ? null : ts.size() == values1.size() ? ev1 : new EnumValues(ts);
  }

  /**
   * Checks whether the given value is a valid enum value.
   * @param value value to be found
   * @return {@code true} if this instance contains the item value
   */
  public boolean matches(final byte[] value) {
    return values.contains(value);
  }

  /**
   * Checks whether all of the values of this object are also values of the given enum values.
   * @param ev enum values to test
   * @return result of check
   */
  public boolean instanceOf(final EnumValues ev) {
    for(final byte[] value : values) {
      if(!ev.values.contains(value)) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for(final byte[] v : values) {
      h = (h << 5) - h + Token.hash(v);
    }
    return h;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof EnumValues)) return false;
    final EnumValues other = (EnumValues) obj;
    if(values.size() != other.values.size() || !instanceOf(other)) return false;
    return true;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(AtomType.ENUM).token('(');
    int n = values.size();
    for(final byte[] value : values) {
      qs.quoted(value);
      if(--n != 0) qs.token(',');
    }
    return qs.token(')').toString();
  }
}
