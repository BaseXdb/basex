package org.basex.core.users;

import org.basex.util.options.*;

/**
 * Codes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Code {
  /** Salt. */ SALT,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return EnumOption.string(this);
  }
}
