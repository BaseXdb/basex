package org.basex.core.proc;

import org.basex.build.fs.FSParser;
import org.basex.build.fs.NewFSParser;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create fs' command and creates a new filesystem mapping
 * from an existing file hierarchy.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Alexander Holupirek
 */
public final class CreateFS extends ACreate {
  /**
   * Default constructor.
   * @param path filesystem path
   * @param name name of database
   */
  public CreateFS(final String path, final String name) {
    super(STANDARD | User.CREATE, path, name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);
    return prop.is(Prop.NEWFSPARSER) ?
      build(new NewFSParser(args[0], prop), args[1]) :
      build(new FSParser(args[0], prop), args[1]);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.FS + args();
  }
}