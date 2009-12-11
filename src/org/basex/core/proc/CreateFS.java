package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.fs.FSParser;
import org.basex.build.fs.FSTraversalParser;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create fs' command and creates a new filesystem mapping from
 * an existing file hierarchy.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Alexander Holupirek
 * @author Bastian Lemke
 */
public final class CreateFS extends ACreate {
  /**
   * Default constructor.
   * @param path filesystem path
   * @param name name of database
   */
  public CreateFS(final String path, final String name) {
    super(User.CREATE, path, name);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);

    final String path;
    try {
      path = new File(args[0]).getCanonicalPath();
    } catch(IOException ex) {
      return error(ex.getMessage());
    }
    final String db = args[1];

    // old FSParser
    if(!prop.is(Prop.FSTRAVERSAL)) return build(new FSParser(path, prop), db);

    // XQUP-based implementation
    FSTraversalParser parser = new FSTraversalParser(path, context, db);
    progress(parser);
    parser.parse();

    final Optimize opt = new Optimize();
    progress(opt);
    opt.execute(context);
    new Open(db).execute(context);
    return info(DBCREATED, args[1], perf);
  }

  @Override
  public void abort() {
    new Open(args[1]).execute(context);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.FS + args();
  }
}
