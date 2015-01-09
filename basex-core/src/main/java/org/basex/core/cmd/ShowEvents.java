package org.basex.core.cmd;

import java.io.*;

import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdShow;

/**
 * Evaluates the 'show events' command and lists all existing events.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Roman Raedle
 * @author Andreas Weiler
 */
public final class ShowEvents extends AEvent {
  @Override
  protected boolean run() throws IOException {
    out.println(context.events.info());
    return true;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.EVENTS).args();
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.EVENT);
  }
}
