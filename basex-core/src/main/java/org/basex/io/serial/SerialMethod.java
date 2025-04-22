package org.basex.io.serial;

import org.basex.util.options.*;

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

  @Override
  public String toString() {
    return EnumOption.string(this);
  }
}
