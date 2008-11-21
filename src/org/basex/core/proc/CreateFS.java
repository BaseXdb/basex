package org.basex.core.proc;

import org.basex.build.fs.FSParser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Creates a new filesystem mapping.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateFS extends ACreate {
  /**
   * Constructor.
   * @param path filesystem path
   * @param name name of database 
   */
  public CreateFS(final String path, final String name) {
    super(STANDARD, path, name);
  }
  
  @Override
  protected boolean exec() {
    Prop.chop = true;
    Prop.entity = true;
    Prop.mainmem = false;
    // if the first argument is a slash, all directories are
    // parsed (which includes all partitions on Windows systems)
    return build(new FSParser(IO.get(args[0]), args[0].equals("/")), args[1]);
  }
  
  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.FS + args();
  }
}
