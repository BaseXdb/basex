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
   * Return the enclosed expression.
   * @return the expression.
   */
  public RegExp getEncl() {
    return encl;
  }

  /**
   * Set the back-reference flag.
   */
  public void setHasBackRef() {
    hasBackRef = true;
  }

  /**
   * Get the back-reference flag.
   * @return the flag value.
   */
  public boolean hasBackRef() {
    return hasBackRef;
  }

  /**
   * Get the atom path of this group.
   * @return the atom path.
   */
  public Integer[] getAtomPath() {
    return atomPath;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(capture ? name != null ? "(?<" + name + '>' : "(" : "(?:");
    encl.toRegEx(sb);
    sb.append(')');
  }
}
