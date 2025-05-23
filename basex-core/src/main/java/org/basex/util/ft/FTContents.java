package org.basex.util.ft;

import org.basex.util.*;

/**
 * Full-text content types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum FTContents {
  /** At start.       */ START,
  /** At end.         */ END,
  /** Entire content. */ ENTIRE;

  /**
   * Returns a string representation.
   * @return string representation
   */
  @Override
  public String toString() {
    return Enums.string(this);
  }
}
