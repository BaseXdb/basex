package org.basex.core.proc;

import org.basex.core.Commands;

/**
 * Evaluates the 'createxml' command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateXML extends Proc {
  @Override
  protected boolean exec() {
    final String path = cmd.args().replace('\\', '/');
    return exec(Commands.CREATE, Create.XML + ' ' + path);
  }
}
