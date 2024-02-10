package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'delete' command and deletes resources from a database.
 *
 * @author BaseX Team 2005-24, BSD License
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
    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);

    return update(data, () -> {
      context.invalidate();

      // delete XML documents
      final IntList docs = data.resources.docs(path);
      int size = docs.size();
      if(size != 0) {
        final AtomicUpdateCache auc = new AtomicUpdateCache(data);
        for(int d = 0; d < size; d++) auc.addDelete(docs.get(d));
        auc.execute(false);
      }
      // delete file resources
      for(final ResourceType type : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, type);
        if(bin != null && bin.exists()) {
          size += bin.isDir() ? bin.descendants().size() : 1;
          bin.delete();
        }
      }
      return info(RES_DELETED_X_X, size, jc().performance);
    });
  }
}
