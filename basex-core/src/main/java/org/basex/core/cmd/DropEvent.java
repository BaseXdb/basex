package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdDrop;

/**
 * Evaluates the 'drop event' command and drops an existing event.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class DropEvent extends AEvent {
  /**
   * Default constructor.
   * @param name user name
   */
  public DropEvent(final String name) {
    super(name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.events.drop(name) ?
        info(EVENT_DROPPED_X, name) : error(EVENT_UNKNOWN_X, name);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.DROP + " " + CmdDrop.EVENT).args();
  }
}
