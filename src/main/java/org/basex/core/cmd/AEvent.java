package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Abstract class for database events.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AEvent extends Command {
  /**
   * Protected constructor.
   * @param a arguments
   */
  protected AEvent(final String... a) {
    super(Perm.ADMIN, false, a);
  }

  @Override
  public boolean databases(final StringList db) {
    db.add(DBLocking.EVENT);
    return true;
  }
}
