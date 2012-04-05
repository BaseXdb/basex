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
   * @param p required permission
   * @param a arguments
   */
  AGet(final Perm p, final String... a) {
    super(p, a);
  }
}
