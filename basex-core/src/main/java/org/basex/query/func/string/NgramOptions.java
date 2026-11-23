package org.basex.query.func.string;

import org.basex.util.options.*;

/**
 * N-gram options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class NgramOptions extends StringOptions {
  /** Option: n-gram length. */
  public static final NumberOption N = new NumberOption("n", 2);
  /** Option: pad the input with boundary characters. */
  public static final BooleanOption PADDING = new BooleanOption("padding", false);
}
