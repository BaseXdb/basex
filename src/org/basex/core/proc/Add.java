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
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'add' command and adds a single document to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    super(DATAREF | User.WRITE, input);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);

    final int pre = findDoc(Token.token(io.name()));
    if(pre != -1) return error(DBDOC, args[0]);

    final DirParser p = new DirParser(io, context.prop);
    MemData d = null;
    try {
      d = new MemBuilder(p).build(io.dbname());
    } catch(final IOException ex) {
      Main.debug(ex);
      final String msg = ex.getMessage();
      return error(msg != null ? msg : args[0]);
    }
    final Data data = context.data;
    data.insert(data.meta.size, -1, d);
    data.flush();
    context.update();
    return true;
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
