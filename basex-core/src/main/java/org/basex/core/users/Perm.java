package org.basex.core.users;

import org.basex.util.*;

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

  /**
   * Checks if this permission includes the specified permission.
   * @param perm permission
   * @return result of check
   */
  public boolean has(final Perm perm) {
    return ordinal() >= perm.ordinal();
  }

  /**
   * Returns the lower of this and the specified permission.
   * @param perm permission
   * @return lower permission
   */
  public Perm min(final Perm perm) {
    return perm.ordinal() < ordinal() ? perm : this;
  }

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
