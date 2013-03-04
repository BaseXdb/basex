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
  AEvent(final String... a) {
    super(Perm.ADMIN, false, a);
  }

  /**
   * Protected constructor.
   * @param d requires opened database
   * @param a arguments
   */
  AEvent(final boolean d, final String... a) {
    super(Perm.ADMIN, d, a);
  }

  @Override
  public boolean databases(final StringList db) {
    db.add(DBLocking.EVENT);
    return true;
  }
}
