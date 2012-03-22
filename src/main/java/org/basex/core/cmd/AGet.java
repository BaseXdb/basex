package org.basex.core.cmd;

import org.basex.core.*;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param f command flags
   * @param a arguments
   */
  AGet(final int f, final String... a) {
    super(f, a);
  }
}
