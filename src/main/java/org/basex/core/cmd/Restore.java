package org.basex.core.cmd;

import org.basex.core.Command;
import org.basex.core.User;

/**
 * Evaluates the 'restore' command restores a backup of a database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Restore extends Command {
  /**
   * Default constructor.
   * @param arg optional argument
   */
  public Restore(final String arg) {
    super(User.CREATE, arg);
  }

  @Override
  protected boolean run() {
    return true;
  }
}
