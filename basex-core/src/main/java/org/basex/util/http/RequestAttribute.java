package org.basex.util.http;

import java.util.*;

/**
 * HTTP Request attributes.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public enum RequestAttribute {
  /** NC. */ NC,
  /** QOP. */ QOP,
  /** URI. */ URI,
  /** Href. */ HREF,
  /** Nonce. */ NONCE,
  /** Realm. */ REALM,
  /** Opaque. */ OPAQUE,
  /** Cnonce. */ CNONCE,
  /** Method. */ METHOD,
  /** Timeout. */ TIMEOUT,
  /** Response. */ RESPONSE,
  /** Password. */ PASSWORD,
  /** Username. */ USERNAME,
  /** Algorithm. */ ALGORITHM,
  /** Auth-method. */ AUTH_METHOD,
  /** Status-only. */ STATUS_ONLY,
  /** Follow-redirect. */ FOLLOW_REDIRECT,
  /** Send-authorization. */ SEND_AUTHORIZATION,
  /** Override-media-type. */ OVERRIDE_MEDIA_TYPE;

  /** Cached enums (faster). */
  public static final RequestAttribute[] VALUES = values();

  /**
   * Returns an enum for the specified string.
   * @param key key
   * @return enum or {@code null}
   */
  public static RequestAttribute get(final String key) {
    for(final RequestAttribute r : VALUES) {
      if(key.equals(r.toString())) return r;
    }
    return null;
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
  }
}
