package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.Command;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param a arguments
   */
  protected AGet(final String... a) {
    super(STANDARD, a);
  }

  /**
   * Creates an error message for an unknown key and returns {@code false}.
   * @return false
   */
  protected final boolean whichKey() {
    final String in = args[0].toUpperCase();
    final String key = prop.similar(in);
    return key != null ? error(SETSIMILAR, in, key) : error(SETWHICH, in);
  }
}
