package org.basex.query.func.fn;

import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class UriOptions extends Options {
  /** Option. */
  public static final StringOption PATH_SEPARATOR =
      new StringOption("path-separator", "/");
  /** Option. */
  public static final StringOption QUERY_SEPARATOR =
      new StringOption("query-separator", "&");
  /** Option. */
  public static final BooleanOption ALLOW_DEPRECATED_FEATURES =
      new BooleanOption("allow-deprecated-features", false);
  /** Option. */
  public static final BooleanOption OMIT_DEFAULT_PORTS =
      new BooleanOption("omit-default-ports", false);
  /** Option. */
  public static final BooleanOption UNC_PATH =
      new BooleanOption("unc-path", false);
}
