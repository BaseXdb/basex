package org.basex.core.users;

import java.util.*;

/** Algorithms. */
public enum Algorithm {
  /** Digest. */
  DIGEST(Code.HASH),
  /** Salted SHA-256. */
  SALTED_SHA256(Code.SALT, Code.HASH);

  /** Used codes. */
  final Code[] codes;

  /**
   * Constructor.
   * @param codes used codes
   */
  Algorithm(final Code... codes) {
    this.codes = codes;
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
  }

  /**
   * Returns an enum matching the specified string.
   * @param string string
   * @return enum
   */
  public static Algorithm get(final String string) {
    for(final Algorithm a : values()) if(a.toString().equals(string)) return a;
    return null;
  }
}
