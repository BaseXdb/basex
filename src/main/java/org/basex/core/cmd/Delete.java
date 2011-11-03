package org.basex.core.cmd;

import static org.basex.util.Token.*;
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

    // delete documents
    final IntList docs = data.docs(target);
    delete(context, docs);
    // delete raw resources
    final TokenList raw = files(data, target);
    delete(data, raw);

    return info(PATHDELETED, docs.size() + raw.size(), perf);
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
  public static byte[] delete(final Data data, final TokenList res) {
    for(final byte[] key : res) {
      final IOFile file = data.meta.binary(string(key));
      if(file == null || !file.delete()) return key;
    }
    return null;
  }

  /**
   * Returns the resources to be deleted.
   * @param data data reference
   * @param res resources to be deleted
   * @return resources
   */
  public static TokenList files(final Data data, final String res) {
    // delete raw resources
    final TokenList tl = data.files(res);
    // if necessary, delete root directory
    final IOFile bin = data.meta.binary(res);
    if(bin != null && bin.isDir()) tl.add(res);
    return tl;
  }
}
