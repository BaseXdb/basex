package org.basex.query.func.ft;

import org.basex.util.options.*;

/**
 * Full-text options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FtThesaurusOptions extends FtIndexOptions {
  /** Option: relationship. */
  public static final StringOption RELATIONSHIP = new StringOption("relationship", "");
  /** Option: levels. */
  public static final NumberOption LEVELS = new NumberOption("levels", Integer.MAX_VALUE);
}
