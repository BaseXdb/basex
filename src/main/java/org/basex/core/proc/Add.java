package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;

/**
 * Evaluates the 'add' command and adds a single document to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Add extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    this(input, null);
  }

  /**
   * Constructor, specifying a target.
   * @param input input XML file or XML string
   * @param ta target
   */
  public Add(final String input, final String ta) {
    super(DATAREF | User.WRITE, input, ta == null ? "" : ta);
  }

  @Override
  protected boolean run() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);

    final String name = io.name();
    final String dbname = io.dbname();

    final DirParser p = new DirParser(io, context.prop, path(args[1]));
    try {
      final MemData d = new MemBuilder(p, context.prop).build(dbname);
      final Data data = context.data;
      data.insert(data.meta.size, -1, d);
      data.flush();
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : name);
    }

    context.update();
    return info(PATHADDED, name, perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
