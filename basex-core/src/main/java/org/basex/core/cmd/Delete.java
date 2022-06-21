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
    return update(data, () -> {
      context.invalidate();

      // delete XML documents
      final IntList docs = data.resources.docs(target);
      int size = docs.size();
      if(size != 0) {
        final AtomicUpdateCache auc = new AtomicUpdateCache(data);
        for(int d = 0; d < size; d++) auc.addDelete(docs.get(d));
        auc.execute(false);
      }

      // delete file resources
      final IOFile bin = data.meta.binary(target);
      if(bin != null && bin.exists()) {
        size += bin.isDir() ? bin.descendants().size() : 1;
        bin.delete();
      }
      return info(RES_DELETED_X_X, size, jc().performance);
    });
  }
}
