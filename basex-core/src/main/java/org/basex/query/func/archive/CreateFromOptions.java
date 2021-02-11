package org.basex.query.func.archive;

import org.basex.util.options.*;

/**
 * Options for processing archives.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CreateFromOptions extends CreateOptions {
  /** Recursive parsing. */
  public static final BooleanOption RECURSIVE = new BooleanOption("recursive", true);
  /** Include root directory. */
  public static final BooleanOption ROOT_DIR = new BooleanOption("root-dir", false);
}
