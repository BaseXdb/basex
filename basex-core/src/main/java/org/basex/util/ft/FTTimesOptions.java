package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text occurrence options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTTimesOptions extends Options {
  /** Option: min. */
  public static final NumberOption MIN = new NumberOption("min", 0);
  /** Option: max. */
  public static final NumberOption MAX = new NumberOption("max", Integer.MAX_VALUE);
}
