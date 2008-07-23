package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DirParser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.index.FTBuilder;
import org.basex.index.FZBuilder;
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
    final IO f = new IO(args[0]);
    if(!f.exists()) return error(FILEWHICH, f);
    final String db = args.length == 1 || args[1] == null ?
        f.dbname() : args[1];
    return build(new DirParser(f), db);
  }

  /**
   * Creates and returns a database for the specified XML document.
   * No warnings are thrown; instead, an empty reference is returned.
   * @param db name of the database to be created
   * @param fn database name
   * @return database instance
   */
  public static Data xml(final IO fn, final String db) {
    try {
      if(!fn.exists()) return null;
      final Parser p = new DirParser(fn);
      if(Prop.onthefly) return new MemBuilder().build(p, db);

      final Data data = new DiskBuilder().build(p, db);
      if(data.meta.txtindex) data.openIndex(
          IndexToken.TYPE.TXT, new ValueBuilder(true).build(data));
      if(data.meta.atvindex) data.openIndex(
          IndexToken.TYPE.ATV, new ValueBuilder(false).build(data));
      if(data.meta.ftxindex) data.openIndex(
          IndexToken.TYPE.FTX, data.meta.ftfuzzy ?
              new FZBuilder().build(data) : new FTBuilder().build(data));
      return data;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return null;
    }
  }
  
  @Override
  public String toString() {
    return COMMANDS.CREATE.name() + " " + CREATE.DB + args();
  }
}
