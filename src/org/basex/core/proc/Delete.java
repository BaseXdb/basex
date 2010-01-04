package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'delete' command and deletes a document from a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Delete extends ACreate {
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Delete(final String input) {
    super(DATAREF | User.WRITE, input);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    final IO io = IO.get(args[0]);
    if(!io.exists()) return error(FILEWHICH, io);

    final int pre = findDoc(Token.token(io.name()));
    if(pre == -1) return error(DBNODOC, args[0]);

    final Data data = context.data;
    data.delete(pre);
    data.flush();
    context.update();
    return info(DOCDELETED, io.name(), perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
