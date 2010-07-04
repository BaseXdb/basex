package org.basex.core.cmd;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;

/**
 * Evaluates the 'checks' command, opens an existing database or
 * creates a new one.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Check extends Command {
  /**
   * Default constructor.
   * @param path file path
   */
  public Check(final String path) {
    super(User.CREATE, path);
  }

  @Override
  protected boolean run() {
    new Close().run(context);

    final String path = args[0];
    final String name = IO.get(path).dbname();
    final Command cmd = MetaData.found(path, name, context.prop) ?
      new Open(name) : new CreateDB(name, path);
    final boolean ok = cmd.run(context);
    final String msg = cmd.info().trim();
    return ok ? info(msg) : error(msg);
  }

  /**
   * Opens the specified database; if it does not exist, create a new
   * database instance.
   * @param ctx database context
   * @param path document path
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data check(final Context ctx, final String path)
      throws IOException {

    final IO io = IO.get(path);
    final String name = io.dbname();
    return MetaData.found(path, name, ctx.prop) ? Open.open(name, ctx) :
      CreateDB.xml(io, name, ctx);
  }
}
