package org.basex.core.users;

import java.util.*;

/** Codes. */
public enum Code {
  /** Salt. */ SALT,
  /** Hash. */ HASH;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
