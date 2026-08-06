package org.basex.query.func.ws;

import org.basex.core.jobs.*;
import org.basex.io.serial.*;
import org.basex.util.options.*;

/**
 * WebSocket eval options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsOptions extends JobOptions {
  /** Parameters for serializing the query result. */
  public static final OptionsOption<SerializerOptions> SERIALIZER =
      new OptionsOption<>("serializer", new SerializerOptions());
}
