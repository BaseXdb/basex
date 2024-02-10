package org.basex.io.serial;

import org.basex.util.options.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team 2005-24, BSD License
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
    return EnumOption.string(name());
  }
}
