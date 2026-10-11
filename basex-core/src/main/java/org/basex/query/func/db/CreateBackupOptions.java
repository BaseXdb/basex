package org.basex.query.func.db;

import org.basex.util.options.*;

/**
 * Options for creating backups.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CreateBackupOptions extends Options {
  /** Comment. */
  public static final StringOption COMMENT = new StringOption("comment");
  /** Compression flag. */
  public static final BooleanOption COMPRESS = new BooleanOption("compress", true);
}
