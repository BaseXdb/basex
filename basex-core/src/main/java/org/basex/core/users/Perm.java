package org.basex.core.users;

import java.util.*;

/**
 * User permissions.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param num numeric representation
   */
  Perm(final int num) {
    this.num = num;
  }

  /**
   * Returns the permission with less privileges.
   * @param perm permission to be compared
   * @return permission
   */
  public Perm min(final Perm perm) {
    return num < perm.num ? this : perm;
  }

  /**
   * Returns the permission with more privileges.
   * @param perm permission to be compared
   * @return permission
   */
  public Perm max(final Perm perm) {
    return num > perm.num ? this : perm;
  }

  /**
   * Returns a permission matching the specified number.
   * @param num numeric representation
   * @return permission, or {@link #ADMIN} if no match is found
   */
  static Perm get(final int num) {
    for(final Perm p : values()) if(p.num == num) return p;
    return ADMIN;
  }

  /**
   * Returns a permission matching the specified string.
   * @param perm permission string
   * @return permission, or {@code null} if no match is found
   */
  public static Perm get(final String perm) {
    for(final Perm p : values()) if(p.name().equalsIgnoreCase(perm)) return p;
    return null;
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
