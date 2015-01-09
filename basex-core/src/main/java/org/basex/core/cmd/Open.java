package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'open' command and opens a database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Open extends Command {
  /**
   * Default constructor.
   * @param name name of database
   */
  public Open(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name name of database
   * @param path database path
   */
  public Open(final String name, final String path) {
    super(Perm.NONE, name, path);
  }

  @Override
  protected boolean run() {
    final String db = args[0];
    if(!Databases.validName(db)) return error(NAME_INVALID_X, db);

    // check if database is already opened
    Data data = context.data();
    if(data == null || !data.meta.name.equals(db)) {
      new Close().run(context);
      try {
        data = open(db, context, options);
        context.openDB(data);

        final String path = args[1];
        if(path != null && !path.isEmpty()) {
          context.current(new DBNodes(data, data.resources.docs(path).toArray()));
        }
        if(data.meta.oldindex()) info(H_INDEX_FORMAT);
        if(data.meta.corrupt)  info(DB_CORRUPT);
      } catch(final IOException ex) {
        return error(Util.message(ex));
      }
    }
    return info(DB_OPENED_X, db, perf);
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX).add(args[0]);
  }

  @Override
  public boolean newData(final Context ctx) {
    return new Close().run(ctx);
  }

  /**
   * Opens the specified database.
   * @param name name of database
   * @param context database context
   * @param options main options
   * @return data reference
   * @throws IOException I/O exception
   */
  public static Data open(final String name, final Context context, final MainOptions options)
      throws IOException {

    // check permissions
    if(!context.perm(Perm.READ, name)) throw new BaseXException(PERM_REQUIRED_X, Perm.READ);

    synchronized(context.dbs) {
      Data data = context.dbs.pin(name);
      if(data == null) {
        // check if the addressed database exists
        if(!context.soptions.dbexists(name)) throw new BaseXException(dbnf(name));

        // do not open a database that is currently updated
        final MetaData meta = new MetaData(name, options, context.soptions);
        if(meta.updateFile().exists()) throw new BaseXException(Text.DB_UPDATED_X, meta.name);

        // open database
        data = new DiskData(meta);
        context.dbs.add(data);
      }
      return data;
    }
  }

  /**
   * Returns an error message for an unknown database.
   * @param name name of database
   * @return error message
   */
  public static String dbnf(final String name) {
    return Util.info(DB_NOT_FOUND_X, name);
  }
}
