package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DirParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.index.FTTrieBuilder;
import org.basex.index.FTFuzzyBuilder;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;

/**
 * Creates a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CreateDB extends ACreate {
  /**
   * Constructor. The file name, excluding the suffix, is used as database name.
   * @param input file name or XML string
   */
  public CreateDB(final String input) {
    this(input, input);
  }

  /**
   * Constructor.
   * @param input file name or XML string
   * @param name name of database; if set to null,
   *         a main memory instance is created
   */
  public CreateDB(final String input, final String name) {
    super(STANDARD, input, name == null ? null : IO.get(name).dbname());
  }

  @Override
  protected boolean exec() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    return build(new DirParser(io), args[1]);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * @param ctx database context
   * @param io file reference
   * @param name name of the database to be created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final Context ctx, final IO io, final String name)
      throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(ctx, new DirParser(io), name);
  }

  /**
   * Creates and returns a database instance for the specified XML document.
   * @param ctx database context
   * @param p xml parser
   * @param db name of the database to be created; if db is <code>null</code>,
   * a main memory instance is created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final Context ctx, final Parser p, final String db)
      throws IOException {
    if(Prop.mainmem) return xml(p);

    final Builder builder = new DiskBuilder();
    try {
      final Data data = builder.build(p, db);
      if(data.meta.txtindex) data.setIndex(Type.TXT,
        new ValueBuilder(true).build(data));
      if(data.meta.atvindex) data.setIndex(Type.ATV,
        new ValueBuilder(false).build(data));
      if(data.meta.ftxindex) data.setIndex(Type.FTX, data.meta.ftfz ?
        new FTFuzzyBuilder().build(data) : new FTTrieBuilder().build(data));

      // [AW] null test should be removed if query processor handles context
      if(ctx != null) ctx.addToPool(data);
      return data;
    } catch(final IOException ex) {
      try {
        builder.close();
      } catch(final IOException e) {
        BaseX.debug(e);
      }
      DropDB.drop(db);
      throw ex;
    }
  }

  /**
   * Creates and returns a main memory database from the specified parser.
   * @param p xml parser
   * a main memory instance is created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final Parser p) throws IOException {
    return new MemBuilder().build(p, "");
  }

  /**
   * Creates and returns a main memory database from the specified XML document.
   * @param io file reference
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final IO io) throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(new DirParser(io));
  }

  /**
   * Creates and returns a main memory database from the specified SAX source.
   * @param s sax source
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final SAXSource s) throws IOException {
    return xml(new SAXWrapper(s));
  }

  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.DB + args();
  }
}
