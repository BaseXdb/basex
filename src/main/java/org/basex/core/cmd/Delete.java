package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import org.basex.core.User;
import org.basex.data.Data;

/**
 * Evaluates the 'delete' command and deletes a document from a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Delete extends ACreate {
  /**
   * Default constructor.
   * @param target target to delete
   */
  public Delete(final String target) {
    super(DATAREF | User.WRITE, target);
  }

  @Override
  protected boolean run() {
    final String target = path(args[0]);
    final byte[] exact = token(target);
    final byte[] pref = token(target + "/");

    int c = 0;
    final Data data = context.data;
    final int[] docs = context.doc();

    // loop through all documents in reverse order
    for(int d = docs.length - 1; d >= 0; d--) {
      final int pre = docs[d];
      final byte[] name = context.data.text(pre, true);
      // delete all exact matches and all sub directories
      if(!eq(name, exact) && !startsWith(name, pref)) continue;
      data.delete(pre);
      c++;
    }
    // data was changed: update context
    if(c != 0) {
      data.flush();
      context.update();
    }
    return info(PATHDELETED, c, perf);
  }
}
