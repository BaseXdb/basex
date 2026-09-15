package org.basex.build.json;

import org.basex.query.func.fn.PlanFn.*;
import org.basex.util.options.*;

/**
 * Options for converting between JSON and XML with the w3-mapping format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonMappingOptions extends ElementsOptions {
  /** Option: name of the root element. */
  public static final StringOption ROOT = new StringOption("root");
}
