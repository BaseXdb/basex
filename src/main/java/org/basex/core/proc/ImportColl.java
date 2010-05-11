package org.basex.core.proc;

import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdImport;

/**
 * Evaluates the 'import coll' command and adds a single document
 * to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class ImportColl extends Add {
  /**
   * Default constructor.
   * @param name of the document
   * @param input input XML string
   */
  public ImportColl(final String name, final String input) {
    // [AW] to be revised... 'import coll' is expected to work on open database
    super(name, input);
  }

  @Override
  public String toString() {
    return Cmd.IMPORT + " " + CmdImport.COLL + args();
  }
}
