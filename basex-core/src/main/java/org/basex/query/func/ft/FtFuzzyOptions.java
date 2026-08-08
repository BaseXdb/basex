package org.basex.query.func.ft;

import org.basex.util.options.*;

/**
 * Full-text fuzzy options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FtFuzzyOptions extends Options {
  /** Option: fuzzy. */
  public static final BooleanOption FUZZY = new BooleanOption("fuzzy", false);
  /** Option: Levenshtein errors. */
  public static final NumberOption ERRORS = new NumberOption("errors");
}
