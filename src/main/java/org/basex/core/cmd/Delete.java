package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.util.list.IntList;
import org.basex.util.list.StringList;

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
    final String target = args[0];
    if(target.endsWith(".")) return error(NAMEINVALID, target);

    // delete documents
    final IntList docs = data.docs(target);
    delete(context, docs);
    // delete raw resources
    final StringList sl = files(data, target);
    delete(data, sl);

    return info(PATHDELETED, docs.size() + sl.size(), perf);
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
   * @param res resources to be deleted
   * @return {@code null}, or the name of a resource that could not be deleted
   */
  public static String delete(final Data data, final StringList res) {
    for(final String key : res) {
      if(!data.meta.binary(key).delete()) return key;
    }
    return null;
  }

  /**
   * Returns the resources to be deleted.
   * @param data data reference
   * @param res resources to be deleted
   * @return resources
   */
  public static StringList files(final Data data, final String res) {
    // delete raw resources
    final StringList sl = data.files(res);
    // if necessary, delete root directory
    final IOFile bin = data.meta.binary(res);
    if(bin.isDir()) sl.add(res);
    delete(data, sl);
    return sl;
  }
}
