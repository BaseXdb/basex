package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.xml.DirParser;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Evaluates the 'add' command and adds a single document to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class Add extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    super(DATAREF | User.WRITE, input);
  }
  
  /**
   * Default constructor.
   * @param input input XML file or XML string
   * @param name name of database
   */
  public Add(final String input, final String name) {
    super(DATAREF | User.WRITE, input, name);
  }

  @Override
  protected boolean run() {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);
    String name;
    String dbname;
    if(io.name().isEmpty()) {
      name = args[1];
      dbname = name;
    } else {
      name = io.name();
      dbname = io.dbname();
    }
    final int pre = findDoc(Token.token(name));
    if(pre != -1) return error(DBDOC, args[0]);

    final DirParser p = new DirParser(io, context.prop);
    MemData d = null;
    try {
      d = new MemBuilder(p).build(dbname);
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    }
    final Data data = context.data;
    data.insert(data.meta.size, -1, d);
    data.flush();
    context.update();
    return info(DOCADDED, name, perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
