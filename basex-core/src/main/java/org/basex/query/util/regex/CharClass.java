package org.basex.query.util.regex;

/**
 * A character class.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class CharClass extends RegExp {
  /** Char group of this class. */
  private final CharGroup group;
  /** Excluded char class (can be {@code null}). */
  private final CharClass subtract;

  /**
   * Constructor.
   * @param group char group
   * @param subtract excluded char class (can be {@code null})
   */
  public CharClass(final CharGroup group, final CharClass subtract) {
    this.group = group;
    this.subtract = subtract;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append('[');
    final boolean wrap = subtract != null && group.negative;
    if(wrap) sb.append('[');
    group.toRegEx(sb);
    if(wrap) sb.append(']');
    // subtraction: intersection with the complement of the (possibly nested) excluded class
    if(subtract != null) {
      subtract.toRegEx(sb.append("&&[^"));
      sb.append(']');
    }
    sb.append(']');
  }
}
