package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.Command;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param f command flags
   * @param a arguments
   */
  protected AGet(final int f, final String... a) {
    super(f, a);
  }

  /**
   * Creates an error message for an unknown key and returns {@code false}.
   * @return false
   */
  protected final boolean whichKey() {
    final String key = args[0].toUpperCase();
    final String sim = prop.similar(key);
    return sim != null ? error(SETSIMILAR, key, sim) : error(SETWHICH, key);
  }
}
