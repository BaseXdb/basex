package org.basex.http;

import org.basex.core.*;

/**
 * This class assembles HTTP properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPProp extends AProp {
  // OPTIONS ============================================================================

  /** RESTXQ path. */
  public static final Object[] RESTXQPATH = { "RESTXQPATH", "" };
  /** Default user. */
  public static final Object[] USER = { "USER", "" };
  /** Default password. */
  public static final Object[] PASSWORD = { "PASSWORD", "" };
  /** Use client/server architecture. */
  public static final Object[] SERVER = { "SERVER", true };
  /** Verbose output. */
  public static final Object[] VERBOSE = { "VERBOSE", false };
}
