package org.basex.util.ft;

import java.io.*;

import org.basex.util.*;

/**
 * This class contains parser options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends Options {
  /** Option: fuzzy. */
  public static final Option FUZZY = new Option("fuzzy", false);
  /** Option: wildcards. */
  public static final Option WILDCARDS = new Option("wildcards", false);
  /** Option: wildcards. */
  public static final Option MODE = new Option("mode", "any");

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
    parse(opts, true);
  }
}
