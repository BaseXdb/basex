package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'check' command: opens an existing database or
 * creates a new one.
 *
 * @author BaseX Team 2005-15, BSD License
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
    proc(cmd);

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
    if(!context.user().has(Perm.CREATE)) return true;
    // database with given name does not exist
    if(!soptions.dbpath(qi.db).exists()) return false;
    // open database if addressed file does not exist
    if(!qi.input.exists()) return true;

    // compare timestamp of database input and specified file
    final MetaData meta = new MetaData(qi.db, options, soptions);
    try {
      meta.read();
      return meta.time == qi.input.timeStamp();
    } catch(final IOException ex) {
      // rebuild database if it cannot be opened
      return false;
    }
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
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX).add(new QueryInput(args[0]).input.dbname());
  }

  @Override
  public boolean newData(final Context ctx) {
    return new Close().run(ctx);
  }
}
