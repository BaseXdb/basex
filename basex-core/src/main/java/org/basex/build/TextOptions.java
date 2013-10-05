package org.basex.build;

import java.io.*;

import org.basex.util.*;

/**
 * This class contains parser options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextOptions extends Options {
  /** Parser option: encoding. */
  public static final Option ENCODING = new Option("encoding", Token.UTF8);
  /** Parser option: line-wise parsing. */
  public static final Option LINES = new Option("lines", true);

  /**
   * Constructor.
   */
  public TextOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws IOException I/O exception
   */
  public TextOptions(final String opts) throws IOException {
    parse(opts, true);
  }
}
