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
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.index.FTTrieBuilder;
import org.basex.index.FTFuzzyBuilder;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;

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
    super(STANDARD, input, IO.get(name == null ? input : name).dbname());
  }

  /**
   * Constructor. The file name, excluding the suffix, is used as database name.
   * @param input input file or XML string
   */
  public CreateDB(final String input) {
    this(input, null);
  }

  @Override
  protected boolean exec() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    return build(new DirParser(io, prop), args[1]);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * @param ctx database context
   * @param io file reference
   * @param name name of the database to be created
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Context ctx, final IO io, final String name)
      throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(ctx, new DirParser(io, ctx.prop), name);
  }

  /**
   * Creates and returns a database instance from the specified parser.
   * @param ctx database context
   * @param p xml parser
   * @param db name of the database to be created; if db is <code>null</code>,
   * a main memory instance is created
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Context ctx, final Parser p, final String db)
      throws IOException {

    final Prop pr = p.prop;
    if(pr.is(Prop.MAINMEM)) new MemBuilder(p).build(db);

    final Builder builder = new DiskBuilder(p);
    final String tmp = db + ".tmp";
    try {
      final Data data = builder.build(tmp);
      if(data.meta.txtindex) data.setIndex(Type.TXT,
        new ValueBuilder(data, true).build());
      if(data.meta.atvindex) data.setIndex(Type.ATV,
        new ValueBuilder(data, false).build());
      if(data.meta.ftxindex) data.setIndex(Type.FTX, data.meta.ftfz ?
        new FTFuzzyBuilder(data, pr).build() :
        new FTTrieBuilder(data, pr).build());
      data.close();
    } catch(final IOException ex) {
      try {
        builder.close();
      } catch(final IOException exx) {
        Main.debug(exx);
      }
      DropDB.drop(tmp, pr);
      throw ex;
    }
    move(db, pr);
    return Open.open(ctx, db);
  }

  /**
   * Creates and returns a main memory database for the specified parser.
   * @param p xml parser
   * @return database instance
   * @throws IOException I/O exception
   */
  public static Data xml(final Parser p) throws IOException {
    return new MemBuilder(p).build();
  }

  /**
   * Creates and returns a main memory database from the specified
   * input reference.
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
   * Creates and returns a main memory database from the specified SAX source.
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
