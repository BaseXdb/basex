package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.FSParser;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;

/**
 * Evaluates the 'create fs' command and creates a new filesystem mapping from
 * an existing file hierarchy.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Alexander Holupirek
 * @author Bastian Lemke
 */
public final class CreateFS extends ACreate {
  /**
   * Default constructor.
   * @param name name of database
   * @param path filesystem path
   */
  public CreateFS(final String name, final String path) {
    super(name, path);
  }

  @Override
  protected boolean run() {
    prop.set(Prop.CHOP, true);
    prop.set(Prop.ENTITY, true);

    final String path;
    try {
      final File f = new File(args[1]).getCanonicalFile();
      if(!f.exists()) return error(FILEWHICH, f.getAbsolutePath());
      path = f.getCanonicalPath();
    } catch(final IOException ex) {
      return error(ex.getMessage());
    }

    final String db = args[0];
    final FSParser fs = new FSParser(path, context, db);
    progress(fs);
    fs.parse();

    final Optimize opt = new Optimize();
    progress(opt);
    if(!opt.run(context)) return error(opt.info());

    final Open pr = new Open(db);
    return pr.run(context) ? info(DBCREATED, db, perf) : error(pr.info());
  }

  @Override
  public void abort() {
    new Open(args[0]).run(context);
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.FS + args();
  }
}
