package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'drop event' command and drops an existing event.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class DropEvent extends Command {
  /**
   * Default constructor.
   * @param name user name
   */
  public DropEvent(final String name) {
    super(Perm.ADMIN, name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.events.drop(name) ?
        info(EVENT_DROPPED_X, name) : error(EVENT_UNKNOWN_X, name);
  }

  @Override
  protected boolean databases(final StringList db) {
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.EVENT).args();
  }
}
