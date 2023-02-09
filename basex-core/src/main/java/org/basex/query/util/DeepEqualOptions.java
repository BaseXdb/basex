package org.basex.query.util;

import org.basex.util.options.*;

/**
 * Options for comparing values.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DeepEqualOptions extends Options {
  /** Option: compare comments. */
  public static final BooleanOption COMMENTS =
      new BooleanOption("comments", false);
  /** Option: compare in-scope-namespaces. */
  public static final BooleanOption NAMESPACES_PREFIXES =
      new BooleanOption("in-scope-namespaces", false);
  /** Option: compare processing instructions. */
  public static final BooleanOption PROCESSING_INSTRUCTIONS =
      new BooleanOption("processing-instructions", false);
}
