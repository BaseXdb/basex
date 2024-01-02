package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.core.users.*;

/**
 * Abstract class for database backup.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class ABackup extends Command {
  /**
   * Protected constructor.
   * @param args arguments
   */
  ABackup(final String... args) {
    super(Perm.CREATE, false, args);
  }
}
