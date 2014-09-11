package org.basex.query.func.archive;

import static org.basex.query.func.archive.ArchiveText.*;

import org.basex.util.options.*;

/**
 * Options for processing archives.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ArchOptions extends Options {
  /** Archiving format. */
  public static final StringOption FORMAT = new StringOption("format", ZIP);
  /** Archiving algorithm. */
  public static final StringOption ALGORITHM = new StringOption("algorithm", DEFLATE);
}
