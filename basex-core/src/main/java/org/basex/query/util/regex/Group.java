package org.basex.query.util.regex;

/**
 * A parenthesized group.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Group extends RegExp {
  /** Enclosed expression. */
  private final RegExp encl;
  /** Capture flag. */
  private final boolean capture;
  /** Group name (can be {@code null}). */
  private final String name;
  /** Back-reference flag. */
  private boolean hasBackRef;
  /** Atom path of this group: sequence numbers of ancestor branches and atoms. */
  private final Integer[] atomPath;
  /** Java number of this group. */
  private int number;
  /** Java number of the marker group ({@code 0} if there is no back-reference). */
  private int marker;

  /**
   * Constructor.
   * @param encl enclosed expression
   * @param capture capture flag
   * @param atomPath atom path of this group
   */
  public Group(final RegExp encl, final boolean capture, final Integer[] atomPath) {
    this(encl, capture, null, atomPath);
  }

  /**
   * Constructor.
   * @param encl enclosed expression
   * @param capture capture flag
   * @param name group name (can be {@code null})
   * @param atomPath atom path of this group
   */
  public Group(final RegExp encl, final boolean capture, final String name,
      final Integer[] atomPath) {
    this.encl = encl;
    this.capture = capture;
    this.name = name;
    hasBackRef = false;
    this.atomPath = atomPath;
  }

  /**
   * Set the back-reference flag.
   */
  public void setHasBackRef() {
    hasBackRef = true;
  }

  /**
   * Get the back-reference flag.
   * @return flag value
   */
  public boolean hasBackRef() {
    return hasBackRef;
  }

  /**
   * Get the atom path of this group.
   * @return atom path
   */
  public Integer[] getAtomPath() {
    return atomPath;
  }

  /**
   * Set the Java number of this group.
   * @param nr number
   */
  public void setNumber(final int nr) {
    number = nr;
  }

  /**
   * Set the Java number of the marker group, which is only captured if this group is.
   * @param nr number
   */
  public void setMarker(final int nr) {
    marker = nr;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    toRegEx(sb, null);
  }

  /**
   * Appends this group, with an optional quantifier applied to its content.
   * @param sb string builder
   * @param quant quantifier (can be {@code null})
   */
  void toRegEx(final StringBuilder sb, final Quantifier quant) {
    sb.append(capture ? name != null ? "(?<" + name + '>' : "(" : "(?:");
    if(quant != null) {
      // #2240: an optional group is captured as empty string in each iteration (W3C bug 29253)
      sb.append("(?:");
      encl.toRegEx(sb);
      sb.append(')');
      quant.toRegEx(sb);
    } else {
      encl.toRegEx(sb);
    }
    if(marker != 0) sb.append("()");
    sb.append(')');
  }

  /**
   * Appends a back-reference to this group, which matches an empty string if the group has not
   * been captured.
   * @param sb string builder
   * @param insensitive case-insensitive matching
   */
  void backRef(final StringBuilder sb, final boolean insensitive) {
    sb.append("(?:");
    if(insensitive) sb.append("(?iu:\\").append(number).append(')');
    else sb.append('\\').append(number);
    sb.append("|(?!\\").append(marker).append("))");
  }
}
