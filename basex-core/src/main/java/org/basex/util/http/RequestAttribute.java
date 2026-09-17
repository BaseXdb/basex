package org.basex.util.http;

import org.basex.util.*;

/**
 * HTTP Request attributes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum RequestAttribute {
  /** CSV. */ CSV,
  /** Href. */ HREF,
  /** JSON. */ JSON,
  /** HTML. */ HTML,
  /** Method. */ METHOD,
  /** Cookies. */ COOKIES,
  /** Timeout. */ TIMEOUT,
  /** Password. */ PASSWORD,
  /** Username. */ USERNAME,
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
