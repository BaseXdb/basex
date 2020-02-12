package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'replace' command and replaces documents in a collection.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class Replace extends ACreate {
  /**
   * Constructor.
   * The input needs to be set via {@link #setInput(InputStream)}.
   * @param path resource path
   */
  public Replace(final String path) {
    this(path, null);
  }

  /**
   * Constructor.
   * @param path resource path
   * @param input input reference (local/remote file path or XML string; can be {@code null})
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
    if(path == null) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    final IOFile bin = data.meta.binary(path);
    if(!data.inMemory() && bin == null) return error(PATH_INVALID_X, args[0]);

    return update(data, new Code() {
      @Override
      boolean run() {
        return replace(data, bin, path);
      }
    });
  }

  /**
   * Replaces files in the specified database.
   * @param data database
   * @param bin binary file (can be {@code null})
   * @param path target path
   * @return success flag
   */
  private boolean replace(final Data data, final IOFile bin, final String path) {
    // retrieve old list of resources
    final AtomicUpdateCache auc = new AtomicUpdateCache(data);

    final IntList docs = data.resources.docs(path);
    int d = 0, bs = 0;
    if(bin != null && bin.exists()) {
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

        final DataClip clip = new DataClip(add.tmpData);
        if(docs.isEmpty()) {
          auc.addInsert(data.meta.size, -1, clip);
        } else {
          auc.addReplace(docs.get(d++), clip);
        }
        context.invalidate();
      } finally {
        add.finish();
      }
    }

    // delete old documents
    final int ds = docs.size();
    for(; d < ds; d++) auc.addDelete(docs.get(d));
    auc.execute(false);

    return info(RES_REPLACED_X_X, ds + bs, jc().performance);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0).add(1);
  }
}
