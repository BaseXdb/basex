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
 * @author BaseX Team 2005-12, BSD License
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
    final IntList pre = data.resources.docs(path, true);

    if(!data.startUpdate()) return error(DB_PINNED_X, data.meta.name);
    try {
      final boolean ok;
      final IOFile file = data.meta.binary(path);
      if(file != null && file.exists()) {
        // replace binary file if it already exists
        final Store store = new Store(path);
        store.setInput(in);
        ok = store.run(context) || error(store.info());
      } else {
        // otherwise, add new document as xml
        final Add add = new Add(path);
        add.setInput(in);
        add.lock = false;
        ok = add.run(context) || error(add.info());
        // delete old documents if addition was successful
        if(ok) {
          final AtomicUpdateList atomics = new AtomicUpdateList(data);
          for(int p = pre.size() - 1; p >= 0; p--) atomics.addDelete(pre.get(p));
          atomics.execute(false);
        }
      }
      return ok && info(RES_REPLACED_X_X, 1, perf);
    } finally {
      data.finishUpdate();
    }
  }
}
