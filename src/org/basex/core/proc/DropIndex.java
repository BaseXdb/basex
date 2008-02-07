package org.basex.core.proc;

import org.basex.core.Commands;

/**
 * Evaluates the 'drop index' command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DropIndex extends Proc {
  @Override
  protected boolean exec() {
    final String path = cmd.args().replace('\\', '/');
    return exec(Commands.DROP, Drop.INDEX + " " + path);
  }
}
