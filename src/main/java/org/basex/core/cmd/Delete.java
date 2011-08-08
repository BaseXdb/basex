package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.util.list.IntList;

/**
 * Evaluates the 'delete' command and deletes documents from a collection.
 *
 * @author BaseX Team 2005-11, BSD License
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
    final Data data = context.data();
    final IntList docs = data.doc(args[0]);
    delete(context, docs);
    return info(PATHDELETED, docs.size(), perf);
  }

  /**
   * Deletes the specified nodes.
   * @param ctx database context
   * @param docs documents to be deleted
   */
  public static void delete(final Context ctx, final IntList docs) {
    // data was changed: update context
    if(docs.size() == 0) return;

    // loop through all documents in reverse order
    final Data data = ctx.data();
    for(int d = docs.size() - 1; d >= 0; d--) data.delete(docs.get(d));
    ctx.update();
    data.flush();
  }
}
