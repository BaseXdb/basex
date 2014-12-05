package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.users.*;
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
    if(path == null || path.isEmpty() || path.endsWith(".")) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    final IOFile bin = data.meta.binary(path);
    if(bin == null) return error(PATH_INVALID_X, args[0]);

    if(!startUpdate()) return false;
    try {
      // retrieve old list of resources
      final AtomicUpdateCache auc = new AtomicUpdateCache(data);

      final IntList docs = data.resources.docs(path);
      int d = 0, bs = 0;
      if(bin.exists()) {
        // replace binary file if it already exists
        final Store store = new Store(path);
        store.setInput(in);
        store.lock = false;
        if(!store.run(context)) return error(store.info());
        bs = 1;
      } else {
        // otherwise, add new document as xml
        final Add add = new Add(path);
        try {
          add.setInput(in);
          add.init(context, out);
          if(!add.build()) return error(add.info());

          if(docs.isEmpty()) {
            auc.addInsert(data.meta.size, -1, add.clip);
          } else {
            auc.addReplace(docs.get(0), add.clip);
            d = 1;
          }

          context.invalidate();
        } finally {
          add.close();
        }
      }

      // delete old documents
      final int ds = docs.size();
      for(; d < ds; d++) auc.addDelete(docs.get(d));
      auc.execute(false);

      return info(RES_REPLACED_X_X, ds + bs, perf);
    } finally {
      if(!finishUpdate()) return false;
    }
  }
}
