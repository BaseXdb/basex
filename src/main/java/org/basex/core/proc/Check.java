package org.basex.core.proc;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Proc;
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
public final class Check extends Proc {
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
    final String db = IO.get(path).dbname();
    final Proc p = MetaData.found(path, db, context.prop) ?
      new Open(db) : new CreateDB(path);
    final boolean ok = p.run(context);
    info(p.info());
    return ok;
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

    final IO f = IO.get(path);
    final String db = f.dbname();
    return MetaData.found(path, db, ctx.prop) ? Open.open(ctx, db) :
      CreateDB.xml(ctx, f, db);
  }
}
