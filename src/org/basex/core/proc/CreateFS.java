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

  /** File hierarchy to be imported. */
  private final String fsimportpath;
  /** Database name for imported file hierarchy. */
  private final String dbname;
  /** DeepFS mount point. */
  private final String mountpoint;
  /** DeepFS backing store. */
  private final String backingstore;

  /**
   * Constructor.
   * @param path filesystem path
   * @param name name of database
   */
  public CreateFS(final String path, final String name) {
    this(path, name, "no_fuse", "no_fuse");
  }

  /**
   * Constructor.
   * @param path filesystem path
   * @param name name of database
   * @param mp fuse mount point
   * @param bp path to BLOB backing store
   */
  public CreateFS(final String path, final String name, final String mp,
      final String bp) {
    super(STANDARD, path, name, mp, bp);
    fsimportpath = path;
    dbname = name;
    mountpoint = mp;
    backingstore = bp;
  }

  @Override
  protected boolean exec() {
    Prop.chop = true;
    Prop.entity = true;
    Prop.mainmem = false;
    return Prop.newfsparser //
    ? build(new NewFSParser(fsimportpath, mountpoint, backingstore), dbname)
        : build(new FSParser(fsimportpath, mountpoint, backingstore), dbname);
  }

  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.FS + args();
  }
}
