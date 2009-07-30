package org.basex.core.proc;

import org.basex.build.fs.FSParser;
import org.basex.build.fs.NewFSParser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Creates a new filesystem mapping.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateFS extends ACreate {
  /**
   * Constructor.
   * @param path filesystem path
   * @param db name of database
   */
  public CreateFS(final String path, final String db) {
    this(path, db, "", "");
  }

  /**
   * Constructor.
   * @param path filesystem path
   * @param db name of database
   * @param mp fuse mount point
   * @param bp path to BLOB backing store
   */
  public CreateFS(final String path, final String db, final String mp,
      final String bp) {
    super(STANDARD, path, db, mp, bp);
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
    return Cmd.CREATE.name() + " " + CmdCreate.FS + args();
  }
}
