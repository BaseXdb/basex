package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DirParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.data.Data.IndexType;
import org.basex.index.FTBuilder;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class CreateDB extends ACreate {
  /**
   * Default constructor.
   * @param name name of database
   */
  public CreateDB(final String name) {
    this(name, null);
  }

  /**
   * Constructor, specifying an input.
   * @param name name of database
   * @param input input file path or XML string
   */
  public CreateDB(final String name, final String input) {
    super(name, input);
  }

  @Override
  protected boolean run() {
    final String name = args[0];
    final String input = args[1];
    if(input == null) return build(Parser.emptyParser(name), name);
    final IO io = IO.get(input);
    return io.exists() ? build(new DirParser(io, prop), name) :
      error(FILEWHICH, io);
  }

  /**
   * Creates an empty database.
   * @param ctx database context
   * @param name name of the database
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data empty(final Context ctx, final String name)
      throws IOException {
    return xml(Parser.emptyParser(name), name, ctx);
  }

  /**
   * Creates a database for the specified file.
   * @param ctx database context
   * @param io input reference
   * @param name name of the database
   * @return database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final Context ctx, final IO io,
      final String name) throws IOException {

    if(!ctx.user.perm(User.CREATE))
      throw new IOException(Main.info(PERMNO, CmdPerm.CREATE));
    if(!io.exists()) throw new BuildException(FILEWHICH, io);
    return xml(new DirParser(io, ctx.prop), name, ctx);
  }

  /**
   * Creates a database instance from the specified parser.
   * @param parser input parser
   * @param db name of the database
   * @param ctx database context
   * @return database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final Parser parser, final String db,
      final Context ctx) throws IOException {

    if(ctx.prop.is(Prop.MAINMEM)) return MemBuilder.build(parser, ctx.prop, db);
    if(ctx.pinned(db)) throw new IOException(Main.info(DBLOCKED, db));

    final Builder builder = new DiskBuilder(parser, ctx.prop);
    try {
      final Data data = builder.build(db);
      if(data.meta.txtindex) data.setIndex(IndexType.TXT,
        new ValueBuilder(data, true).build());
      if(data.meta.atvindex) data.setIndex(IndexType.ATV,
        new ValueBuilder(data, false).build());
      if(data.meta.ftxindex) data.setIndex(IndexType.FTX,
        FTBuilder.get(data, data.meta.wildcards).build());
      data.close();
    } catch(final IOException ex) {
      try {
        builder.close();
      } catch(final IOException exx) {
        Main.debug(exx);
      }
      throw ex;
    }
    return Open.open(ctx, db);
  }

  /**
   * Creates a main-memory database for the specified parser.
   * @param parser input parser
   * @param ctx database context
   * @return database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final Parser parser, final Context ctx)
      throws IOException {
    return MemBuilder.build(parser, ctx.prop);
  }

  /**
   * Creates a main-memory database from the specified input reference.
   * @param io input reference
   * @param ctx database context
   * @return database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final IO io, final Context ctx)
      throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(new DirParser(io, ctx.prop), ctx);
  }

  /**
   * Creates a main-memory database from the specified SAX source.
   * @param sax sax source
   * @param ctx database context
   * @return database instance
   * @throws IOException I/O exception
   */
  public static synchronized Data xml(final SAXSource sax, final Context ctx)
      throws IOException {
    return xml(new SAXWrapper(sax, "") , ctx);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.DB).args();
  }
}
