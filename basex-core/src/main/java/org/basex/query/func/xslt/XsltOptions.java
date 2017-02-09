package org.basex.query.func.xslt;

import org.basex.util.options.*;

/**
 * Options for processing archives.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class XsltOptions extends Options {
  /** Cache flag. */
  public static final BooleanOption CACHE = new BooleanOption("cache", false);
}
