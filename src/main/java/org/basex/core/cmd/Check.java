package org.basex.core.cmd;

import static org.basex.data.DataText.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.util.list.*;

/**
 * Evaluates the 'check' command: opens an existing database or
 * creates a new one.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Check extends Command {
  /**
   * Default constructor.
   * @param path file path
   */
  public Check(final String path) {
    super(Perm.NONE, path);
  }

  @Override
  protected boolean run() {
    // close existing database
    new Close().run(context);

    // get path and database name
    final QueryInput qi = new QueryInput(args[0]);
    qi.db = qi.input.dbname();

    // choose OPEN if user has no create permissions, or if database exists
    final Command cmd;
    if(open(qi)) {
      cmd = new Open(qi.db);
    } else {
      cmd = new CreateDB(qi.db, qi.input.exists() ? qi.original : null);
    }
    progress(cmd);

    // execute command
    final boolean ok = cmd.run(context);
    final String msg = cmd.info().trim();
    return ok ? info(msg) : error(msg);
  }

  /**
   * Checks if the addressed database can simply be opened, or needs to be (re)built.
   * @param qi query input
   * @return result of check
   */
  private boolean open(final QueryInput qi) {
    // minimum permissions: create
    if(!context.user.has(Perm.CREATE)) return true;
    // database with given name does not exist
    if(!mprop.dbpath(qi.db).exists()) return false;

    DataInput di = null;
    try {
      final MetaData meta = new MetaData(qi.db, context);
      di = new DataInput(meta.dbfile(DATAINF));
      meta.read(di);
      return meta.time == qi.input.timeStamp();
    } catch(final IOException ignored) {
      // rebuild database if it cannot be opened
    } finally {
      if(di != null) try { di.close(); } catch(final IOException ignored) { }
    }
    return false;
  }

  @Override
  public boolean supportsProg() {
    return true;
  }

  @Override
  public boolean stoppable() {
    return true;
  }

  @Override
  public boolean databases(final StringList db) {
    db.add("").add(new QueryInput(args[0]).input.dbname());
    return true;
  }

  @Override
  public boolean newData(final Context ctx) {
    return new Close().run(ctx);
  }
}
