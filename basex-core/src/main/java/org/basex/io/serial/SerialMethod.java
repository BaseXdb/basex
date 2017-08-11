package org.basex.io.serial;

import java.util.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team 2005-17, BSD License
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

  /** Cached enums (faster). */
  public static final SerialMethod[] VALUES = values();

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Returns a serialization method matching the specified string.
   * @param value value to be found
   * @return serialization method, or {@code null} if no match is found
   */
  public static SerialMethod get(final String value) {
    for(final SerialMethod sm : VALUES) if(sm.toString().equals(value)) return sm;
    return null;
  }
}
