package org.basex.core.users;

import org.basex.util.options.*;

/**
 * Algorithms.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return EnumOption.string(name());
  }
}
