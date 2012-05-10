package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import javax.xml.transform.sax.*;

import org.basex.build.*;
import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.data.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.*;
import org.basex.index.value.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * @param p input parser
   */
  public void setParser(final Parser p) {
    parser = p;
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    if(!MetaData.validName(name, false)) return error(NAME_INVALID_X, name);

    // choose parser and input
    IO io = null;
    final String format = prop.get(Prop.PARSER);
    if(args.length < 1 || args[1] == null) {
      if(in != null && in.getByteStream() != null) {
        try {
          io = cacheRaw();
          if(io == null) {
            InputStream is = in.getByteStream();
            if(!(is instanceof BufferedInputStream ||
                is instanceof BufferInput)) is = new BufferedInputStream(is);

            final LookupInput li = new LookupInput(is);
            if(li.lookup() != -1) {
              parser = new SAXWrapper(new SAXSource(new InputSource(li)),
                  name + '.' + format, context.prop);
            }
          }
        } catch(final IOException ex) {
          Util.debug(ex);
          return error(Util.message(ex));
        }
      }
    } else {
      io = IO.get(args[1]);
    }

    if(io != null) {
      if(!io.exists()) return error(RESOURCE_NOT_FOUND_X, io);
      if(io instanceof IOContent) io.name(name + '.' + format);
      parser = new DirParser(io, prop, mprop.dbpath(name));
    }
    if(parser == null) parser = Parser.emptyParser(context.prop);

    // close open database
    new Close().run(context);

    try {
      if(prop.is(Prop.MAINMEM)) {
        // create main memory instance
        final Data data = progress(new MemBuilder(name, parser)).build();
        context.openDB(data);
        context.pin(data);
      } else {
        if(context.pinned(name)) return error(DB_PINNED_X, name);

        // create disk-based instance
        progress(new DiskBuilder(name, parser, context)).build().close();
        // second step: open database and create index structures
        final Open open = new Open(name);
        if(!open.run(context)) return error(open.info());
        final Data data = context.data();
        try {
          if(data.meta.createtext) create(IndexType.TEXT,      data, this);
          if(data.meta.createattr) create(IndexType.ATTRIBUTE, data, this);
          if(data.meta.createftxt) create(IndexType.FULLTEXT,  data, this);
        } finally {
          data.finishUpdate();
        }
        context.databases().add(name);
      }
      return info(parser.info() + DB_CREATED_X_X, name, perf);
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final IOException ex) {
      Util.debug(ex);
      abort();
      final String msg = ex.getMessage();
      return error(msg != null && !msg.isEmpty() ? msg :
        Util.info(NOT_PARSED_X, parser.src));
    } catch(final Exception ex) {
      // known exceptions:
      // - IllegalArgumentException (UTF8, zip files)
      Util.debug(ex);
      abort();
      return error(Util.info(NOT_PARSED_X, parser.src));
    }
  }

  /**
   * Returns a database instance from the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final String name, final Parser parser,
      final Context ctx) throws IOException {

    // check permissions
    if(!ctx.user.has(Perm.CREATE)) throw new BaseXException(PERM_NEEDED_X, Perm.CREATE);

    // create main memory database instance
    final Prop prop = ctx.prop;
    if(prop.is(Prop.MAINMEM)) return MemBuilder.build(name, parser);

    // database is currently locked by another process
    if(ctx.pinned(name)) throw new BaseXException(DB_PINNED_X, name);

    // create disk builder, set database path
    final Builder builder = new DiskBuilder(name, parser, ctx);

    // build database and index structures
    try {
      final Data data = builder.build();
      if(data.meta.createtext) data.setIndex(IndexType.TEXT,
        new ValueBuilder(data, true).build());
      if(data.meta.createattr) data.setIndex(IndexType.ATTRIBUTE,
        new ValueBuilder(data, false).build());
      if(data.meta.createftxt) data.setIndex(IndexType.FULLTEXT,
        new FTBuilder(data).build());
      data.close();
    } finally {
      try { builder.close(); } catch(final IOException exx) { Util.debug(exx); }
    }
    ctx.databases().add(name);
    return Open.open(name, ctx);
  }

  /**
   * Returns a main memory database instance from the specified parser.
   * @param parser input parser
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized MemData mainMem(final Parser parser, final Context ctx)
      throws IOException {

    if(ctx.user.has(Perm.CREATE)) return MemBuilder.build(parser);
    throw new BaseXException(PERM_NEEDED_X, Perm.CREATE);
  }

  /**
   * Returns a main memory database instance for the specified input reference.
   * @param source document source
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized MemData mainMem(final IO source, final Context ctx)
      throws IOException {

    if(!source.exists()) throw new BaseXException(RESOURCE_NOT_FOUND_X, source);
    return mainMem(new DirParser(source, ctx.prop, null), ctx);
  }

  /**
   * Creates a new database if a valid path was specified.
   * @param source document source
   * @param single expect single document
   * @param ctx database context
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data create(final IO source, final boolean single,
      final Context ctx) throws IOException {

    // check if input is an existing file
    if(!source.exists() || single && source.isDir())
      throw new BaseXException(RESOURCE_NOT_FOUND_X, source);

    // default: create a main memory instance
    if(!ctx.prop.is(Prop.FORCECREATE)) return CreateDB.mainMem(source, ctx);

    // otherwise, create a persistent database instance
    final String nm = source.dbname();
    final DirParser dp = new DirParser(source, ctx.prop, ctx.mprop.dbpath(nm));
    return CreateDB.create(nm, dp, ctx);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.DB).args();
  }
}
