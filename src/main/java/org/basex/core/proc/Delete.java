package org.basex.core.proc;

import static org.basex.core.Text.*;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
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
  protected boolean run() {
    // if arg references real file delete the filename
    // add etc/xml/input.xml > delete etc/xml/input.xml
    final byte[] del = IO.get(args[0]).exists() ?
        Token.token(IO.get(args[0]).name())
        : Token.token(args[0]);

    int pre = -1, count = -1;

    do {
      pre = findDoc(del);
      count++;
      if(pre == -1) break;
      final Data data = context.data;
      data.delete(pre);
      data.flush();
      context.update();
    }while(true);
    
    //[MS] Add number of deleted documents to info
    return (count == 0) ? error(DBNODOC, args[0]) : info(DOCDELETED, del, perf);
  }

  @Override
  public boolean updating(final Context ctx) {
    return true;
  }
}
