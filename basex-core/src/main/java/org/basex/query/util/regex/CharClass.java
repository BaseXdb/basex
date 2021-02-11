package org.basex.query.util.regex;

/**
 * A character class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class CharClass extends RegExp {
  /** Char group of this class. */
  private final CharGroup group;
  /** Excluded char class, possibly {@code null}. */
  private final CharClass subtract;

  /**
   * Constructor.
   * @param group char group
   * @param subtract excluded char class, possibly {@code null}
   */
  public CharClass(final CharGroup group, final CharClass subtract) {
    this.group = group;
    this.subtract = subtract;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    group.toRegEx(sb.append('['));
    if(subtract != null) {
      subtract.group.negative ^= true;
      subtract.toRegEx(sb.append("&&"));
      subtract.group.negative ^= true;
    }
    sb.append(']');
  }
}
