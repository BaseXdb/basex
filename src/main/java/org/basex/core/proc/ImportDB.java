package org.basex.core.proc;

import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdImport;

/**
 * Evaluates the 'import db' command and adds a single document
 * to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class ImportDB extends CreateDB {
  
  /**
   * Default constructor.
   * @param input input XML string
   * @param name name of database
   */
  public ImportDB(final String input, final String name) {
    super(input, name);
  }
  
  @Override
  public String toString() {
    return Cmd.IMPORT + " " + CmdImport.DB + " " + args[1] + " " + args[0];
  }
}
