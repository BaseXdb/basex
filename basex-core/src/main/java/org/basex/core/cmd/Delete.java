package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'delete' command and deletes resources from a database.
 *
 * @author BaseX Team 2005-22, BSD License
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
    return update(data, new Code() {
      @Override
      boolean run() {
        // delete XML documents
        final IntList docs = data.resources.docs(target);
        final AtomicUpdateCache auc = new AtomicUpdateCache(data);
        int size = docs.size();
        for(int d = 0; d < size; d++) auc.addDelete(docs.get(d));
        auc.execute(false);
        context.invalidate();

        // delete binaries
        if(!data.inMemory()) {
          final IOFile path = data.meta.binary(target);
          size += path.isDir() ? path.descendants().size() : 1;
          path.delete();
        }
        return info(RES_DELETED_X_X, size, jc().performance);
      }
    });
  }
}
