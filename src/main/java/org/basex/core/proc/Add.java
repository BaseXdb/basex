package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.data.Data;
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
   * @param name name of document
   * @param target target in collection
   * @param input input XML file or XML string
   */
  public Add(final String name, final String target, final String input) {
    super(DATAREF | User.WRITE, name, target, input);
  }
  
  /**
   * Constructor, specifying no target.
   * @param name name of document
   * @param input input XML file or XML string
   */
  public Add(final String name, final String input) {
    this(name, "", input);
  }

  @Override
  protected boolean run() {
    final IO io = IO.get(args[2]);
    if(!io.exists()) return error(FILEWHICH, io);

    final String name = args[0];
    final String db = args[0];
    io.setName(name);

    final DirParser p = new DirParser(io, context.prop, path(args[1]));
    try {
      final Data data = context.data;
      data.insert(data.meta.size, -1, MemBuilder.build(p, context.prop, db));
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
