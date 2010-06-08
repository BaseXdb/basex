package org.basex.core.proc;

import org.basex.build.mediovis.MAB2Parser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.IO;

/**
 * Evaluates the 'create mab' command and creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CreateMAB extends Create {
  /**
   * Default constructor.
   * @param input input MAB2 file
   * @param name database name
   */
  public CreateMAB(final String input, final String name) {
    super(new MAB2Parser(input), name == null ? IO.get(input).dbname() : name);
  }

  @Override
  protected boolean run() {
    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);
    prop.set(Prop.PATHINDEX, true);
    prop.set(Prop.TEXTINDEX, true);
    prop.set(Prop.ATTRINDEX, true);
    prop.set(Prop.FTINDEX, true);
    ((MAB2Parser) parser).flat = prop.is(Prop.MAB2FLAT);
    return super.run();
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.MAB + args();
  }
}
