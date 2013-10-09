package org.basex.util.ft;

import java.io.*;

import org.basex.util.options.*;

/**
 * Full-text options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends Options {
  /** Option: fuzzy. */
  public static final BooleanOption FUZZY = new BooleanOption("fuzzy", false);
  /** Option: wildcards. */
  public static final BooleanOption WILDCARDS = new BooleanOption("wildcards", false);
  /** Option: wildcards. */
  public static final StringOption MODE = new StringOption("mode", "any");

  /**
   * Constructor.
   */
  public FTOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws IOException I/O exception
   */
  public FTOptions(final String opts) throws IOException {
    super(opts);
  }
}
