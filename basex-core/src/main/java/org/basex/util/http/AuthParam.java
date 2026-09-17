package org.basex.util.http;

import org.basex.util.*;

/**
 * Parameters of HTTP authentication challenges and credentials.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum AuthParam {
  /** NC. */ NC,
  /** QOP. */ QOP,
  /** URI. */ URI,
  /** Nonce. */ NONCE,
  /** Realm. */ REALM,
  /** Scheme. */ SCHEME,
  /** Opaque. */ OPAQUE,
  /** Cnonce. */ CNONCE,
  /** Response. */ RESPONSE,
  /** Username. */ USERNAME,
  /** Algorithm. */ ALGORITHM;

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
