package org.basex.core.proc;

import static org.basex.Text.*;

import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;

/**
 * Evaluates the 'check' command. Checks if the specified XML document is in
 * memory; if negative, the database file is opened; if it doesn't exist, a
 * new database instance is created.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Check extends Process {
  /**
   * Constructor.
   * @param name name of database
   */
  public Check(final String name) {
    super(STANDARD, name);
  }
  
  @Override
  protected boolean exec() {
    final Data data = context.data();
    final String name = args[0];

    // check open database...
    if(data != null && data.meta.dbname.equals(new IO(name).dbname()))
        return Prop.info ? info(DBINMEM) : true;

    // open or create new database
    return !Prop.onthefly && exec(new Open(name)) || exec(new CreateDB(name));
  }
  
  /**
   * Static command for getting a database reference.
   * No warnings are thrown; instead, an empty reference is returned.
   * @param path file path
   * @return data instance
   */
  public static Data check(final String path) {
    final IO file = new IO(path);
    final String db = file.dbname();
    return MetaData.found(path, db) ? Open.open(db) : CreateDB.xml(file, db);
  }
}
