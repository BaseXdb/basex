package org.basex.core.users;

import org.basex.util.*;

/**
 * Codes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Code {
  /** Salt. */ SALT,
  /** Iterations. */ ITERATIONS,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
