package org.basex.util.ft;

import java.util.*;

/**
 * Full-text diacritics.
 *
 * @author BaseX Team 2005-21, BSD License
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
    return name().toLowerCase(Locale.ENGLISH);
  }
}
