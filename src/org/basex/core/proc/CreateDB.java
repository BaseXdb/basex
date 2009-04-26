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
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Data.Type;
import org.basex.index.FTBuilder;
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
    this(input, null);
  }

  /**
   * Constructor.
   * @param input file name or XML string
   * @param name name of database
   */
  public CreateDB(final String input, final String name) {
    super(STANDARD, input, name);
  }
  
  @Override
  protected boolean exec() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    return build(new DirParser(io), args[1] == null ? io.dbname() : args[1]);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * @param io file reference
   * @param name name of the database to be created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final IO io, final String name) throws IOException {
    if(!io.exists()) throw new BuildException(FILEWHICH, io.path());
    return xml(new DirParser(io), name);
  }

  /**
   * Creates and returns a database from the specified SAX source.
   * @param s sax source
   * @param name name of database
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final SAXSource s, final String name)
      throws IOException {
    return xml(new SAXWrapper(s), name);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * If building fails, an empty reference is returned.
   * @param p xml parser
   * @param db name of the database to be created; if db is <code>null</code>,
   * a main memory instance is created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final Parser p, final String db) throws IOException {
    if(db == null || Prop.onthefly) return new MemBuilder().build(p, "");

    final Builder builder = new DiskBuilder();
    try {
      final Data data = builder.build(p, db);
      if(data.meta.txtindex) data.setIndex(Type.TXT,
        new ValueBuilder(true).build(data));
      if(data.meta.atvindex) data.setIndex(Type.ATV,
        new ValueBuilder(false).build(data));
      if(data.meta.ftxindex) data.setIndex(Type.FTX, data.meta.ftfz ?
        new FTFuzzyBuilder().build(data) : new FTBuilder().build(data));
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
  
  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.DB + args();
  }
}
