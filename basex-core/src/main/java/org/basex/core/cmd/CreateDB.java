package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param input input reference (local/remote file or XML string)
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
        parser = new DirParser(io, context, goptions.dbpath(name));
      } else if(parser == null) {
        parser = Parser.emptyParser(context.options);
      }

      // close open database
      new Close().run(context);

      if(options.get(MainOptions.MAINMEM)) {
        // create main memory instance
        final Data data = proc(new MemBuilder(name, parser)).build();
        context.openDB(data);
        context.dbs.add(data);
      } else {
        if(context.pinned(name)) return error(DB_PINNED_X, name);

        // create disk-based instance
        proc(new DiskBuilder(name, parser, context)).build().close();

        // second step: open database and create index structures
        final Open open = new Open(name);
        if(!open.run(context)) return error(open.info());

        final Data data = context.data();
        if(!startUpdate()) return false;
        try {
          if(data.meta.createtext) create(IndexType.TEXT,      data, options, this);
          if(data.meta.createattr) create(IndexType.ATTRIBUTE, data, options, this);
          if(data.meta.createftxt) create(IndexType.FULLTEXT,  data, options, this);

          // for testing purposes
          final Class<?> luceneClass = Reflect.find("org.basex.modules.LuceneIndex");
          if(luceneClass != null) {
            Util.errln("Creating Lucene Index...");
            final Method m = Reflect.method(luceneClass, "luceneIndex", Context.class);
            Reflect.invoke(m, null, context);
          }

        } finally {
          finishUpdate();
        }
      }
      if(options.get(MainOptions.CREATEONLY)) new Close().run(context);

      return info(parser.info() + DB_CREATED_X_X, name, perf);
    } catch(final ProcException ex) {
      throw ex;
    } catch(final IOException ex) {
      abort();
      return error(Util.message(ex));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      return error(NOT_PARSED_X, parser.source);
    }
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(DBLocking.CTX);
    lr.write.add(args[0]);
  }

  /**
   * Returns a database instance from the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final String name, final Parser parser, final Context ctx)
      throws IOException {
    return create(name, parser, ctx, ctx.options.get(MainOptions.MAINMEM));
  }

  /**
   * Creates a new database instance, using the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @param mem create main-memory instance
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final String name, final Parser parser, final Context ctx,
      final boolean mem) throws IOException {

    // check permissions
    if(!ctx.user.has(Perm.CREATE)) throw new BaseXException(PERM_REQUIRED_X, Perm.CREATE);

    // create main memory database instance
    if(mem) return MemBuilder.build(name, parser);

    // database is currently locked by another process
    if(ctx.pinned(name)) throw new BaseXException(DB_PINNED_X, name);

    // create disk-based instance
    new DiskBuilder(name, parser, ctx).build().close();

    final Data data = Open.open(name, ctx);
    final MainOptions options = ctx.options;
    if(data.meta.createtext) create(IndexType.TEXT,      data, options, null);
    if(data.meta.createattr) create(IndexType.ATTRIBUTE, data, options, null);
    if(data.meta.createftxt) create(IndexType.FULLTEXT,  data, options, null);
    return data;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.DB).args();
  }
}
