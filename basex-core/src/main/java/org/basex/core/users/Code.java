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
  /** Memory size in KiB. */ MEMORY,
  /** Parallelism. */ PARALLELISM,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
