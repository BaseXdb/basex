package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'replace' command and replaces documents in a collection.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Replace extends ACreate {
  /**
   * Constructor.
   * The input needs to be set via {@link #setInput(InputStream)}.
   * @param path resource path
   */
  public Replace(final String path) {
    super(Perm.WRITE, true, path);
  }

  /**
   * Constructor.
   * @param path resource path
   * @param input input file or XML string
   */
  public Replace(final String path, final String input) {
    super(Perm.WRITE, true, path, input);
  }

  @Override
  protected boolean run() {
    // check if the input source has already been initialized
    if(in == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists()) return error(RES_NOT_FOUND_X, io);
      in = io.inputSource();
    }

    final String path = MetaData.normPath(args[0]);
    if(path == null || path.isEmpty()) return error(NO_DIR_ALLOWED_X, args[0]);

    final Data data = context.data();

    if(!startUpdate()) return false;
    try {
      final IOFile file = data.inMemory() ? null : data.meta.binary(path);
      int sz = 1;
      if(file != null && file.exists()) {
        // replace binary file if it already exists
        final Store store = new Store(path);
        store.setInput(in);
        store.lock = false;
        if(!store.run(context)) return error(store.info());
      } else {
        // otherwise, add new document as xml
        final Add add = new Add(path);
        try {
          add.setInput(in);
          add.init(context, out);
          if(!add.build()) return error(add.info());

          // retrieve old list of resources
          final AtomicUpdateCache auc = new AtomicUpdateCache(data);
          final IntList docs = data.resources.docs(path, false);
          sz = docs.size();
          if(docs.isEmpty()) {
            auc.addInsert(data.meta.size, -1, add.clip);
          } else {
            auc.addReplace(docs.get(0), add.clip);
            for(int d = 1; d < sz; d++) auc.addDelete(docs.get(d));
          }

          context.invalidate();
          auc.execute(false);
        } finally {
          add.close();
        }
      }
      return info(RES_REPLACED_X_X, sz, perf);
    } finally {
      finishUpdate();
    }
  }
}
