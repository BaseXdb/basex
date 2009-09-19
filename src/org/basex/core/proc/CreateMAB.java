package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.build.mediovis.MAB2Parser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Evaluates the 'create mab' command and creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateMAB extends ACreate {
  /**
   * Default constructor.
   * @param input input MAB2 file
   * @param name database name
   */
  public CreateMAB(final String input, final String name) {
    super(STANDARD, input, name == null ? IO.get(input).dbname() : name);
  }

  @Override
  protected boolean exec() {
    // check if file exists
    final IO file = IO.get(args[0]);
    if(!file.exists()) return error(FILEWHICH, file);

    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);
    prop.set(Prop.PATHINDEX, true);
    prop.set(Prop.TEXTINDEX, true);
    prop.set(Prop.ATTRINDEX, true);
    prop.set(Prop.FTINDEX, true);
    return build(new MAB2Parser(file, prop), args[1]);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.MAB + args();
  }
}
