package org.basex.core.cmd;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'exit' command and quits the console.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Exit extends Command {
  /** Constructor. */
  public Exit() {
    super(Perm.NONE);
  }

  @Override
  protected boolean run() {
    return new Close().run(context);
  }

  @Override
  public boolean databases(final StringList db) {
    db.add("");
    return true;
  }
}
