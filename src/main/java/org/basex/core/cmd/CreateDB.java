package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.Builder;
import org.basex.build.DirParser;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.BaseXException;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ft.FTBuilder;
import org.basex.index.value.ValueBuilder;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.in.BufferInput;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.xml.sax.InputSource;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CreateDB extends ACreate {
  /** Optionally defined parser. */
  private final Parser parser;

  /**
   * Default constructor.
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
    this(name, input, null);
  }

  /**
   * Constructor, specifying an initial database input.
   * @param name name of database
   * @param input input reference (local/remote file or XML string)
   * @param p parser reference
   */
  public CreateDB(final String name, final String input, final Parser p) {
    super(name, input);
    parser = p;
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    final String input = args[1];
    if(input == null) return build(Parser.emptyParser(), name);
    final IO io = IO.get(input);
    if(io instanceof IOContent) io.name(name + IO.XMLSUFFIX);
    final Parser p = parser != null ? parser : new DirParser(io, prop);
    return io.exists() ? build(p, name) : error(FILEWHICH, io);
  }

  /**
   * Creates a database instance from the specified input stream
   * and assigns it to the specified database context.
   * @param name name of the database
   * @param input input stream
   * @param ctx database context
   * @return info string
   * @throws BaseXException database exception
   */
  public static synchronized String create(final String name,
      final InputStream input, final Context ctx) throws BaseXException {

    final InputStream is = input instanceof BufferedInputStream ||
      input instanceof BufferInput ? input : new BufferedInputStream(input);
    final SAXSource sax = new SAXSource(new InputSource(is));
    return create(name, new SAXWrapper(sax, name + IO.XMLSUFFIX, "",
        ctx.prop), ctx);
  }

  /**
   * Creates a database instance from the specified parser
   * and assigns it to the specified database context.
   * @param name name of the database
   * @param parser parser
   * @param ctx database context
   * @return info string
   * @throws BaseXException database exception
   */
  public static synchronized String create(final String name,
      final Parser parser, final Context ctx) throws BaseXException {

    final Performance p = new Performance();
    ctx.register(true);
    try {
      // close current database instance
      final Data data = ctx.data();
      if(data != null) {
        Close.close(data, ctx);
        ctx.closeDB();
      }
      ctx.openDB(xml(name, parser, ctx));
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    } finally {
      ctx.unregister(true);
    }
    return Util.info(DBCREATED, name, p);
  }

  /**
   * Returns an empty database instance.
   * @param name name of the database
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static Data empty(final String name, final Context ctx)
      throws IOException {
    return xml(name, Parser.emptyParser(), ctx);
  }

  /**
   * Returns a database instance for the specified input.
   * @param name name of the database
   * @param source document source
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final String name, final IO source,
      final Context ctx) throws IOException {

    if(!source.exists()) throw new FileNotFoundException(
        Util.info(FILEWHICH, source));
    return xml(name, new DirParser(source, ctx.prop), ctx);
  }

  /**
   * Returns a database instance from the specified parser.
   * @param name name of the database
   * @param parser input parser
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final String name, final Parser parser,
      final Context ctx) throws IOException {

    // check permissions
    if(!ctx.user.perm(User.CREATE))
      throw new IOException(Util.info(PERMNO, CmdPerm.CREATE));

    // create main memory database instance
    final Prop prop = ctx.prop;
    if(prop.is(Prop.MAINMEM)) return MemBuilder.build(name, parser, ctx.prop);

    // database is currently locked by another process
    if(ctx.pinned(name)) throw new IOException(Util.info(DBLOCKED, name));

    // build database and index structures
    final Builder builder = new DiskBuilder(name, parser, ctx);
    try {
      final Data data = builder.build();
      if(prop.is(Prop.TEXTINDEX)) data.setIndex(IndexType.TEXT,
        new ValueBuilder(data, true).build());
      if(prop.is(Prop.ATTRINDEX)) data.setIndex(IndexType.ATTRIBUTE,
        new ValueBuilder(data, false).build());
      if(prop.is(Prop.FTINDEX))   data.setIndex(IndexType.FULLTEXT,
        FTBuilder.get(data).build());
      data.close();
    } finally {
      try { builder.close(); } catch(final IOException exx) { Util.debug(exx); }
    }
    return Open.open(name, ctx);
  }

  /**
   * Returns a main memory database instance from the specified parser.
   * @param parser input parser
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final Parser parser, final Context ctx)
      throws IOException {

    if(!ctx.user.perm(User.CREATE))
      throw new IOException(Util.info(PERMNO, CmdPerm.CREATE));
    return MemBuilder.build(parser, ctx.prop);
  }

  /**
   * Returns a main memory database instance for the specified input reference.
   * @param source document source
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final IO source, final Context ctx)
      throws IOException {
    if(!source.exists()) throw new FileNotFoundException(
        Util.info(FILEWHICH, source));
    return xml(new DirParser(source, ctx.prop), ctx);
  }

  /**
   * Returns a main memory database instance from the specified SAX source.
   * @param source sax source
   * @param ctx database context
   * @return new database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final SAXSource source, final Context ctx)
      throws IOException {
    return xml(new SAXWrapper(source, ctx.prop) , ctx);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.DB).args();
  }
}
