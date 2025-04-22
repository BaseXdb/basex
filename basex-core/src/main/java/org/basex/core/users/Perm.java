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

  @Override
  public String toString() {
    return EnumOption.string(this);
  }
}
