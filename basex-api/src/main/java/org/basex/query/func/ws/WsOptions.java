package org.basex.query.func.ws;

import org.basex.util.options.*;

/**
 * WebSocket eval options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WsOptions extends Options {
  /** Query base-uri. */
  public static final StringOption BASE_URI = new StringOption("base-uri");
  /** Custom id string. */
  public static final StringOption ID = new StringOption("id");
}
