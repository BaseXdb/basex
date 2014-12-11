package org.basex.core.users;

import java.util.*;

/**
 * Algorithms.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
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
}
