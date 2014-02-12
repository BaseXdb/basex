package org.basex.core;

/**
 * User permissions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public enum Perm {
  /** No permissions. */
  NONE(0),
  /** Read permission (local+global). */
  READ(1),
  /** Write permission (local+global). */
  WRITE(2),
  /** Create permission (global). */
  CREATE(4),
  /** Admin permission (global). */
  ADMIN(8);

  /** Permission. */
  public final int num;

  /**
   * Constructor.
   * @param n numeric representation
   */
  Perm(final int n) {
    num = n;
  }

  /**
   * Returns the permission with less privileges.
   * @param p permission to be compared
   * @return permission
   */
  public Perm min(final Perm p) {
    return num < p.num ? this : p;
  }

  /**
   * Returns the permission with more privileges.
   * @param p permission to be compared
   * @return permission
   */
  public Perm max(final Perm p) {
    return num > p.num ? this : p;
  }

  /**
   * Returns a permission matching the specified number.
   * @param n numeric representation
   * @return permission, or {@link #ADMIN} if no match is found
   */
  static Perm get(final int n) {
    for(final Perm p : values()) if(p.num == n) return p;
    return ADMIN;
  }

  /**
   * Returns a permission matching the specified string.
   * @param s permission string
   * @return permission, or {@link #ADMIN} if no match is found
   */
  public static Perm get(final String s) {
    for(final Perm p : values()) if(p.name().equalsIgnoreCase(s)) return p;
    return ADMIN;
  }
}
