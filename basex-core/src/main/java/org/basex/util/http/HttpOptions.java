package org.basex.util.http;

import org.basex.util.options.*;

/**
 * Options for the functions of the HTTP Client Module 2.0.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HttpOptions extends Options {
  /** Follow redirects. */
  public static final BooleanOption FOLLOW_REDIRECT = new BooleanOption("follow-redirect", true);
  /** Timeout in seconds. */
  public static final NumberOption TIMEOUT = new NumberOption("timeout");
}
