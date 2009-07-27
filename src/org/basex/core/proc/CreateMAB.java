package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.build.mediovis.MAB2Parser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);
    prop.set(Prop.TEXTINDEX, true);
    prop.set(Prop.ATTRINDEX, true);
    prop.set(Prop.FTINDEX, true);
    final String db = args.length > 1 ? args[1] : null;
    return build(new MAB2Parser(file, prop),
        db == null ? file.dbname() : db);
  }

  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.MAB + args();
  }
}
