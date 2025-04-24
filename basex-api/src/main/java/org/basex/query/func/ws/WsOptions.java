package org.basex.query.func.ws;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * WebSocket eval options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsOptions extends Options {
  /** Query base-uri. */
  public static final StringOption BASE_URI = CommonOptions.BASE_URI;
  /** Custom ID string. */
  public static final StringOption ID = new StringOption("id");
}
