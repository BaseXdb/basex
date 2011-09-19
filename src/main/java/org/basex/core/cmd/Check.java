package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.basex.build.DirParser;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * Evaluates the 'checks' command, opens an existing database or
 * creates a new one.
 *
 * @author BaseX Team 2005-11, BSD License
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
    final Command cmd = MetaData.found(path, name, mprop) ?
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

    // check if database is already opened
    final Data data = ctx.pin(name);
    if(data != null) {
      final IO in = IO.get(data.meta.original);
      final boolean found = !data.meta.original.isEmpty() && in.eq(io) &&
        io.date() == in.date();
      if(found && ctx.perm(User.READ, data.meta)) return data;
      Close.close(data, ctx);
      if(found) throw new IOException(Util.info(PERMNO, CmdPerm.READ));
    }

    // open database if it already exists
    if(MetaData.found(path, name, ctx.mprop)) return Open.open(name, ctx);

    // if force flag is set to false, create a main memory instance
    if(!ctx.prop.is(Prop.FORCECREATE)) return CreateDB.mainMem(io, ctx);

    // otherwise, create a persistent database instance
    if(!io.exists()) throw new FileNotFoundException(Util.info(FILEWHICH, io));
    return CreateDB.create(name, new DirParser(io, ctx.prop), ctx);
  }
}
