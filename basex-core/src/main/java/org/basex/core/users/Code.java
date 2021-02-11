package org.basex.core.users;

import java.util.*;

/**
 * Codes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum Code {
  /** Salt. */ SALT,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
