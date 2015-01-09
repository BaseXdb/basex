package org.basex.io.serial;

import java.util.*;

/**
 * Serialization methods.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum SerialMethod {
  /** XML.   */ XML,
  /** XHTML. */ XHTML,
  /** HTML.  */ HTML,
  /** Text.  */ TEXT,
  /** Json.  */ JSON,
  /** CSV.   */ CSV,
  /** RAW.   */ RAW;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}