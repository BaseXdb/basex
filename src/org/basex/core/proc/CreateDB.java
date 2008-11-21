package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DirParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.FTBuilder;
import org.basex.index.FTFuzzyBuilder;
import org.basex.index.IndexToken;
import org.basex.index.ValueBuilder;
import org.basex.io.IO;

/**
 * Creates a new database.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CreateDB extends ACreate {
  /**
   * Constructor.
   * @param input input path
   */
  public CreateDB(final String input) {
    this(input, null);
  }

  /**
   * Constructor.
   * @param input input path
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
   * @param db name of the database to be created
   * @param io file name
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final IO io, final String db) throws IOException {
    return xml(new DirParser(io), db);
  }

  /**
   * Creates and returns a database from the specified SAX source.
   * @param s sax source
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final SAXSource s) throws IOException {
    return xml(new SAXWrapper(s), "tmp");
  }

  /**
   * Creates and returns a database for the specified XML document.
   * If building fails, an empty reference is returned.
   * @param p xml parser
   * @param db name of the database to be created
   * @return database instance
   * @throws IOException exception
   */
  public static Data xml(final Parser p, final String db) throws IOException {
    if(Prop.onthefly) return new MemBuilder().build(p, db);

    final Data data = new DiskBuilder().build(p, db);
    if(data.meta.txtindex) data.setIndex(
        IndexToken.Type.TXT, new ValueBuilder(true).build(data));
    if(data.meta.atvindex) data.setIndex(
        IndexToken.Type.ATV, new ValueBuilder(false).build(data));
    if(data.meta.ftxindex) data.setIndex(
        IndexToken.Type.FTX, data.meta.ftfz ?
          new FTFuzzyBuilder().build(data) : new FTBuilder().build(data));
    return data;
  }
  
  @Override
  public String toString() {
    return Cmd.CREATE.name() + " " + CmdCreate.DB + args();
  }
}
