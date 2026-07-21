package org.basex.io.serial;

import org.basex.util.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum SerialMethod {
  /** XML.      */ XML,
  /** XHTML.    */ XHTML,
  /** HTML.     */ HTML,
  /** JSON.     */ JSON,
  /** CSV.      */ CSV,
  /** Text.     */ TEXT,
  /** Adaptive. */ ADAPTIVE,
  /** BaseX.    */ BASEX;

  /**
   * Checks if this is one of the specified candidates.
   * @param candidates candidates
   * @return result of check
   */
  public boolean oneOf(final SerialMethod... candidates) {
    return Enums.oneOf(this, candidates);
  }

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
