package org.basex.core.proc;

import org.basex.build.fs.FSParser;
import org.basex.build.fs.NewFSParser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Evaluates the 'create fs' command and creates a new filesystem mapping.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateFS extends ACreate {
  /**
   * Default constructor.
   * @param path filesystem path
   * @param name name of database
   */
  public CreateFS(final String path, final String name) {
    this(path, name, "", "");
  }

  /**
   * Constructor, specifying FUSE properties.
   * @param path filesystem path
   * @param name name of database
   * @param mp fuse mount point
   * @param bs path to BLOB backing store
   */
  public CreateFS(final String path, final String name, final String mp,
      final String bs) {
    super(STANDARD, path, name, mp, bs);
  }

  @Override
  protected boolean exec() {
    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);
    return prop.is(Prop.NEWFSPARSER) ?
      build(new NewFSParser(args[0], args[2], args[3], prop), args[1]) :
      build(new FSParser(args[0], args[2], args[3], prop), args[1]);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.FS + args();
  }
}
