package org.basex.query.util.regex;

/**
 * A parenthesized group.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class Group extends RegExp {
  /** Enclosed expression. */
  private final RegExp encl;
  /** Capture flag. */
  private final boolean capture;
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
    this.encl = encl;
    this.capture = capture;
    this.hasBackRef = false;
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
    this.hasBackRef = true;
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
    sb.append(capture ? "(" : "(?:");
    encl.toRegEx(sb);
    sb.append(')');
  }
}
