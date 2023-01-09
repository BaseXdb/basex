package org.basex.query.func.db;

import org.basex.util.options.*;

/**
 * Options for processing archives.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CreateBackupOptions extends Options {
  /** Recursive parsing. */
  public static final StringOption COMMENT = new StringOption("comment");
  /** Include root directory. */
  public static final BooleanOption COMPRESS = new BooleanOption("compress", true);
}
