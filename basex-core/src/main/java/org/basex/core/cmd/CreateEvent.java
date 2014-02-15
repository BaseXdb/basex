package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;

/**
 * Evaluates the 'create event' command and creates a new event.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class CreateEvent extends AEvent {
  /**
   * Default constructor.
   * @param name user name
   */
  public CreateEvent(final String name) {
    super(name);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    return context.events.create(name) ?
        info(EVENT_CREATED_X, name) : error(EVENT_EXISTS_X, name);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.EVENT).args();
  }
}
