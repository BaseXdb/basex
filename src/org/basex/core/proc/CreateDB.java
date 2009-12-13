package org.basex.core.proc;

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
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.index.FTBuilder;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;
import org.basex.io.PrintOutput;

/**
 * Evaluates the 'create db' command and creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateDB extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   * @param name name of database
   * (special characters are stripped before the name is applied)
   */
  public CreateDB(final String input, final String name) {
    super(User.CREATE, input, IO.get(name == null ? input : name).dbname());
  }

  /**
   * Constructor. The file name, excluding the suffix, is used as database name.
   * @param input input file or XML string
   */
  public CreateDB(final String input) {
    this(input, null);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final IO io = IO.get(args[0]);
    return io.exists() ? build(new DirParser(io, prop), args[1]) :
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
    return xml(ctx, Parser.emptyParser(IO.get(name), ctx.prop), name);
  }

  /**
   * Creates a database for the specified file.
   * @param ctx database context
   * @param io file reference
   * @param name name of the database
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Context ctx, final IO io, final String name)
      throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH,
        ctx.user.perm(User.ADMIN) ? io : io.name());
    return xml(ctx, new DirParser(io, ctx.prop), name);
  }

  /**
   * Creates a database instance from the specified parser.
   * @param ctx database context
   * @param p xml parser
   * @param db name of the database
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Context ctx, final Parser p, final String db)
      throws IOException {

    if(ctx.prop.is(Prop.MAINMEM)) return new MemBuilder(p).build(db);
    if(ctx.pinned(db)) throw new IOException(Main.info(DBLOCKED, db));

    final Builder builder = new DiskBuilder(p);
    try {
      final Data data = builder.build(db);
      if(data.meta.txtindex) data.setIndex(Type.TXT,
        new ValueBuilder(data, true).build());
      if(data.meta.atvindex) data.setIndex(Type.ATV,
        new ValueBuilder(data, false).build());
      if(data.meta.ftxindex) data.setIndex(Type.FTX,
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
   * Creates a main memory database for the specified parser.
   * @param p xml parser
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Parser p) throws IOException {
    return new MemBuilder(p).build();
  }

  /**
   * Creates a main memory database from the specified input reference.
   * @param io file reference
   * @param pr database properties
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final IO io, final Prop pr) throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(new DirParser(io, pr));
  }

  /**
   * Creates a main memory database from the specified SAX source.
   * @param s sax source
   * @param pr database properties
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final SAXSource s, final Prop pr) throws IOException {
    return xml(new SAXWrapper(s, pr));
  }

  @Override
  public String toString() {
    return Cmd.CREATE + " " + CmdCreate.DB + args();
  }
}
