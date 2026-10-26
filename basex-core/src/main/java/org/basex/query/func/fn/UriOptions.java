package org.basex.query.func.fn;

import org.basex.util.options.*;

/**
 * Options for fn:parse-uri and fn:build-uri.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UriOptions extends Options {
  /** Option. */
  public static final BooleanOption ALLOW_DEPRECATED_FEATURES =
      new BooleanOption("allow-deprecated-features", false);
  /** Option. */
  public static final BooleanOption OMIT_DEFAULT_PORTS =
      new BooleanOption("omit-default-ports", false);
  /** Option. */
  public static final BooleanOption UNC_PATH = new BooleanOption("unc-path", false);
}
