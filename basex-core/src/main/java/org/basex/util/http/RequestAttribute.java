package org.basex.util.http;

import org.basex.util.*;

/**
 * HTTP Request attributes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum RequestAttribute {
  /** NC. */ NC,
  /** CSV. */ CSV,
  /** QOP. */ QOP,
  /** URI. */ URI,
  /** Href. */ HREF,
  /** JSON. */ JSON,
  /** HTML. */ HTML,
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

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
