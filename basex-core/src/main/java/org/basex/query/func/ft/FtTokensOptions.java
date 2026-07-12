package org.basex.query.func.ft;

import org.basex.util.options.*;

/**
 * Options for looking up full-text tokens.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtTokensOptions extends Options {
  /** Option: fuzzy. */
  public static final BooleanOption FUZZY = new BooleanOption("fuzzy", false);
  /** Option: Levenshtein errors. */
  public static final NumberOption ERRORS = new NumberOption("errors");
}
