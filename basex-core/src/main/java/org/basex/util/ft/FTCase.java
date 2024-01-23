package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text cases.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public enum FTCase {
  /** Sensitive.   */ SENSITIVE,
  /** Insensitive. */ INSENSITIVE,
  /** Lower-case.  */ LOWER,
  /** Upper-case.  */ UPPER;

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return EnumOption.string(name());
  }
}
