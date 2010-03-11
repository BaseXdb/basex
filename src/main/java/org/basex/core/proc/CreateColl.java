package org.basex.core.proc;

import org.basex.build.Parser;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Evaluates the 'create coll' command and creates a new collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CreateColl extends ACreate {
  /**
   * Default constructor.
   * @param name name of database
   * (special characters are stripped before the name is applied)
   */
  public CreateColl(final String name) {
    super(User.CREATE, name);
  }

  @Override
  protected boolean run() {
    final IO io = IO.get(args[0]);
    return build(Parser.emptyParser(io, prop), args[0]);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.COLL + args();
  }
}
