package org.basex.io.serial;

import java.util.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team 2005-21, BSD License
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
}
