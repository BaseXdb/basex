package org.basex.core.proc;

import org.basex.build.fs.FSParser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Creates a new filesystem mapping.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateFS extends ACreate {
  
  /** File hierarchy to be imported. */
  private String fsimportpath;
  /** Database name for imported file hierarchy. */
  private String dbname;
  /** DeepFS mount point. */
  private String mountpoint;
  /** DeepFS backing store. */
  private String backingstore;
  
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
    return build(
        new FSParser(IO.get(fsimportpath), mountpoint, backingstore), dbname);
  }
  
  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.FS + args();
  }
}
