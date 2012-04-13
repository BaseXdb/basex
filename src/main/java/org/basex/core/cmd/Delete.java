package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

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
    super(Perm.WRITE, true, target);
  }

  @Override
  protected boolean run() {
    final Data data = context.data();
    final String target = args[0];

    // start update
    if(!data.startUpdate()) return error(DB_PINNED_X, data.meta.name);

    // delete all documents in reverse order (faster)
    final IntList docs = data.resources.docs(target);
    for(int d = docs.size() - 1; d >= 0; d--) data.delete(docs.get(d));
    if(!docs.isEmpty()) context.update();

    // delete binaries
    final TokenList bins = data.resources.binaries(target);
    delete(data, target);

    // finish update
    data.finishUpdate();

    // return info message
    return info(DOCS_DELETED_X_X, docs.size() + bins.size(), perf);
  }

  /**
   * Deletes the specified resources.
   * @param data data reference
   * @param res resource to be deleted
   */
  public static void delete(final Data data, final String res) {
    final IOFile file = data.meta.binary(res);
    if(file != null) file.delete();
  }
}
