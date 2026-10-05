package org.basex.util.http;

import java.util.*;

/**
 * Authentication schemes of the HTTP client.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum AuthScheme {
  /** Basic.  */ BASIC,
  /** Digest. */ DIGEST;

  @Override
  public String toString() {
    final String name = name();
    return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
  }
}
