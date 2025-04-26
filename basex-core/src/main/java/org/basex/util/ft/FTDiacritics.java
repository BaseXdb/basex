package org.basex.util.ft;

import org.basex.util.*;

/**
 * Full-text diacritics.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum FTDiacritics {
  /** Sensitive.   */ SENSITIVE,
  /** Insensitive. */ INSENSITIVE;

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return Enums.string(this);
  }
}
