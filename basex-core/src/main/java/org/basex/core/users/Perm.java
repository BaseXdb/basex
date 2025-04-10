package org.basex.core.users;

import org.basex.util.options.*;

/**
 * User permissions.
 *
 * @author BaseX Team, BSD License
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

  /** Cached enums (faster). */
  public static final Perm[] VALUES = values();

  /**
   * Returns a permission matching the specified string.
   * @param perm permission string
   * @return permission, or {@code null} if no match is found
   */
  public static Perm get(final String perm) {
    for(final Perm p : VALUES) {
      if(p.toString().equals(perm)) return p;
    }
    return null;
  }

  @Override
  public String toString() {
    return EnumOption.string(this);
  }
}
