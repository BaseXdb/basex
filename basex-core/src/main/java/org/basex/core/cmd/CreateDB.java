package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class CreateDB extends ACreate {
  /** Parser instance. */
  private Parser parser;

  /**
   * Default constructor.
   * Input can be set via {@link #setInput(InputStream)}.
   * @param name name of database
   */
  public CreateDB(final String name) {
    this(name, null);
  }

  /**
   * Constructor, specifying an initial database input.
   * @param name name of database
   * @param input input reference (local/remote file or XML string; can be {@code null})
   */
  public CreateDB(final String name, final String input) {
    super(name, input);
  }

  /**
   * Attaches a parser.
   * @param prsr input parser
   */
  public void setParser(final Parser prsr) {
    parser = prsr;
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!Databases.validName(name)) return error(NAME_INVALID_X, name);

    // choose parser and input
    IO io;
    try {
      io = sourceToIO(name);
      if(in != null) {
        final LookupInput li = new LookupInput(io.inputStream());
        io = li.lookup() == -1 ? null : new IOStream(li, io.name());
      }
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }

    try {
      // create parser instance
      if(io != null) {
        if(!io.exists()) return error(RES_NOT_FOUND_X, io);
        parser = new DirParser(io, options, soptions.dbPath(name));
      } else if(parser == null) {
        parser = Parser.emptyParser(options);
      }

      // close open database
      new Close().run(context);

      final Data data;
      if(options.get(MainOptions.MAINMEM)) {
        // create main memory instance
        try {
          data = pushJob(new MemBuilder(name, parser)).build();
        } finally {
          popJob();
        }
        context.openDB(data);
        context.datas.pin(data);
      } else {
        if(context.pinned(name)) return error(DB_PINNED_X, name);

        // create disk-based instance
        try {
          pushJob(new DiskBuilder(name, parser, soptions, options)).build().close();
        } finally {
          popJob();
        }

        // second step: open database and create index structures
        final Open open = new Open(name);
        if(!open.run(context)) return error(open.info());

        data = context.data();
      }

      if(!startUpdate(data)) return false;
      try {
        CreateIndex.create(data, this);
      } finally {
        if(!finishUpdate(data)) return false;
      }

      if(options.get(MainOptions.CREATEONLY)) new Close().run(context);

      return info(parser.info() + DB_CREATED_X_X, name, job().performance);
    } catch(final JobException ex) {
      throw ex;
    } catch(final IOException ex) {
      abort();
      return error(Util.message(ex));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.stack(ex);
      abort();
      return error(NOT_PARSED_X, parser.source);
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CONTEXT);
    lr.write.add(args[0]);
  }

  /**
   * Returns a database instance from the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @param options main options
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final String name, final Parser parser, final Context ctx,
      final MainOptions options) throws IOException {
    return create(name, parser, ctx, options, options.get(MainOptions.MAINMEM));
  }

  /**
   * Creates a new database instance, using the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @param options main options
   * @param mem create main-memory instance
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final String name, final Parser parser, final Context ctx,
      final MainOptions options, final boolean mem) throws IOException {

    // check permissions
    if(!ctx.user().has(Perm.CREATE)) throw new BaseXException(PERM_REQUIRED_X, Perm.CREATE);

    // create main-memory or disk-based database instance
    final Data data;
    if(mem) {
      data = MemBuilder.build(name, parser);
    } else {
      // database is currently locked by another job
      if(ctx.pinned(name)) throw new BaseXException(DB_PINNED_X, name);
      new DiskBuilder(name, parser, ctx.soptions, options).build().close();
      data = Open.open(name, ctx, options);
    }

    CreateIndex.create(data, null);
    return data;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.DB).args();
  }
}
