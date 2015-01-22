package org.basex.io.serial;

import java.util.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum SerialMethod {
  /** XML.      */ XML,
  /** XHTML.    */ XHTML,
  /** HTML.     */ HTML,
  /** JSON.     */ JSON,
  /** CSV.      */ CSV,
  /** Text.     */ TEXT,
  /** Raw.      */ RAW,
  /** Adaptive. */ ADAPTIVE;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}