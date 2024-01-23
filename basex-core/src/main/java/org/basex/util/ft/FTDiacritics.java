package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text diacritics.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return EnumOption.string(name());
  }
}
