package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.util.list.IntList;
import org.basex.util.list.TokenList;

/**
 * Evaluates the 'delete' command and deletes resources from a database.
 *
 * @author BaseX Team 2005-12, BSD License
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
    final String target = args[0];

    // delete documents
    final IntList docs = data.resources.docs(target);
    delete(context, docs);
    // delete binaries
    final TokenList bins = data.resources.binaries(target);
    delete(data, target);

    return info(PATHDELETED, docs.size() + bins.size(), perf);
  }

  /**
   * Deletes the specified nodes.
   * @param ctx database context
   * @param docs pre values of documents to be deleted
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

  /**
   * Deletes the specified resources.
   * @param data data reference
   * @param res resource to be deleted
   * @return success flag
   */
  public static boolean delete(final Data data, final String res) {
    final IOFile file = data.meta.binary(res);
    return file != null && file.delete();
  }
}
