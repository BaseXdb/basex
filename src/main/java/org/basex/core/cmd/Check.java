package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;

/**
 * Evaluates the 'checks' command, opens an existing database or
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
    final String path = args[0];
    final String name = IO.get(path).dbname();

    // choose OPEN if user has no create permissions, or if database exists
    final boolean create = context.user.has(Perm.CREATE);
    final Command cmd = !create || MetaData.found(path, name, mprop) ?
      new Open(name) : new CreateDB(name, path);

    // execute command
    final boolean ok = cmd.run(context);
    final String msg = cmd.info().trim();
    return ok ? info(msg) : error(msg);
  }

  /**
   * Opens the specified database; create a new one if it does not exist.
   * @param ctx database context
   * @param input document path
   * @param path optional path to the addressed sub-directory. Set to {@code null}
   *        if a single document is addressed
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data check(final Context ctx, final String input,
      final String path) throws IOException {

    // don't create new database if user has insufficient permissions
    final boolean create = ctx.user.has(Perm.CREATE);

    IO io = IO.get(input);
    final String name = io.dbname();

    // check if database is already opened
    final Data data = ctx.pin(name);
    if(data != null) {
      final IO in = IO.get(data.meta.original);
      final boolean found = !data.meta.original.isEmpty() && in.eq(io) &&
        io.timeStamp() == in.timeStamp();
      if(found && ctx.perm(Perm.READ, data.meta)) return data;
      Close.close(data, ctx);
      if(found) throw new BaseXException(PERM_NEEDED_X, Perm.READ);
    }

    // choose OPEN if user has no create permissions, or if database exists
    if(!create || MetaData.found(input, name, ctx.mprop)) return Open.open(name, ctx);

    // check if input is an existing file
    if(path != null) io = IO.get(io + "/" + path);
    if(!io.exists() || path == null && io.isDir())
      throw new BaseXException(RESOURCE_NOT_FOUND_X, io);

    // if force flag is set to false, create a main memory instance
    if(!ctx.prop.is(Prop.FORCECREATE)) return CreateDB.mainMem(io, ctx);

    // otherwise, create a persistent database instance
    final DirParser dp = new DirParser(io, ctx.prop, ctx.mprop.dbpath(name));
    return CreateDB.create(name, dp, ctx);
  }
}
