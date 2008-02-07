package org.basex.core.proc;

import org.basex.core.Commands;

/**
 * Evaluates the 'create index' command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateFS extends Proc {
  @Override
  protected boolean exec() {
    return exec(Commands.CREATE, Create.FS + " " + cmd.args());
  }
}
