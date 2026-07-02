package org.basex.query.util.regex;

/**
 * A group that forces case-sensitive matching, emitted as {@code (?-i:...)}. Used to shield
 * category escapes such as {@code \p{Lu}} from the {@code i} flag, which per the XPath/XQuery
 * F&amp;O rules leaves such constructs unaffected.
 *
 * @author BaseX Team, BSD License
 */
public final class CaseSensitive extends RegExp {
  /** Enclosed expression. */
  private final RegExp encl;

  /**
   * Constructor.
   * @param encl enclosed expression
   */
  public CaseSensitive(final RegExp encl) {
    this.encl = encl;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    encl.toRegEx(sb.append("(?-i:"));
    sb.append(')');
  }
}
