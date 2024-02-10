package org.basex.core.users;

import org.basex.util.options.*;

/**
 * Codes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum Code {
  /** Salt. */ SALT,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return EnumOption.string(name());
  }
}
