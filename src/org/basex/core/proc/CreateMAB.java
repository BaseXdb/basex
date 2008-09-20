package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.build.mediovis.MAB2Parser;
import org.basex.core.Prop;
import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.CREATE;
import org.basex.io.IO;

/**
 * Creates a new database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateMAB extends ACreate {
  /**
   * Constructor.
   * @param p database path
   * @param n database name
   */
  public CreateMAB(final String p, final String n) {
    super(STANDARD, p, n);
  }
  
  @Override
  protected boolean exec() {
    // check if file exists
    final IO file = IO.get(args[0]);
    if(!file.exists()) return error(FILEWHICH, file);

    Prop.chop  = true;
    Prop.entity   = true;
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.ftindex = true;
    return build(new MAB2Parser(file), file.dbname().replaceAll("\\..*", ""));
  }
  
  @Override
  public String toString() {
    return COMMANDS.CREATE.name() + " " + CREATE.MAB + args();
  }
}
