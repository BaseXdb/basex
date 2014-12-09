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
  NONE,
  /** Read permission (local+global). */
  READ,
  /** Write permission (local+global). */
  WRITE,
  /** Create permission (global). */
  CREATE,
  /** Admin permission (global). */
  ADMIN;

  /**
   * Returns the permission with more privileges.
   * @param perm permission to be compared
   * @return permission
   */
  public Perm max(final Perm perm) {
    return ordinal() > perm.ordinal() ? this : perm;
  }

  /**
   * Returns a permission matching the specified string.
   * @param perm permission string
   * @return permission, or {@code null} if no match is found
   */
  public static Perm get(final String perm) {
    for(final Perm p : values()) if(p.toString().equals(perm)) return p;
    return null;
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
