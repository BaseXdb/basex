package org.basex.query.regex;

/**
 * A character class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class CharClass extends RegExp {
  /** Char group of this class. */
  private final CharGroup group;
  /** Excluded char class, possibly {@code null}. */
  private final CharClass subtract;

  /**
   * Constructor.
   * @param grp char group
   * @param sub excluded char class, possibly {@code null}
   */
  public CharClass(final CharGroup grp, final CharClass sub) {
    group = grp;
    subtract = sub;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    group.toRegEx(sb.append('['));
    if(subtract != null) {
      subtract.group.negative ^= true;
      subtract.toRegEx(sb.append("&&"));
      subtract.group.negative ^= true;
    }
    return sb.append(']');
  }
}
