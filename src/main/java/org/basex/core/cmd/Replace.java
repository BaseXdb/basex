package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOFile;

/**
 * Evaluates the 'replace' command and replaces documents in a collection.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Replace extends ACreate {
  /**
   * Default constructor.
   * @param path resource path
   * @param input input file or XML string
   */
  public Replace(final String path, final String input) {
    super(DATAREF | User.WRITE, path, input);
  }

  /**
   * Constructor.
   * @param path resource path
   */
  public Replace(final String path) {
    super(DATAREF | User.WRITE, path);
  }

  @Override
  protected boolean run() {
    // check if the input source has already been initialized
    if(in == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists()) return error(FILE_NOT_FOUND_X, io);
      in = io.inputSource();
    }

    final String path = MetaData.normPath(args[0]);
    if(path == null || path.isEmpty()) return error(NO_DIR_ALLOWED_X, args[0]);

    final Data data = context.data();
    final int pre = data.resources.doc(path, false);
    // check if path points to a single file
    if(pre != -1 && data.resources.docs(path).size() != 1)
      return error(NO_DIR_ALLOWED_X, path);

    final IOFile file = data.meta.binary(path);
    if(file != null && file.exists()) {
      // replace binary file if it already exists
      final Store store = new Store(path);
      store.setInput(in);
      if(!store.run(context)) return error(store.info());
    } else {
      // otherwise, add new document as xml
      final Add add = new Add(path);
      add.setInput(in);
      if(!add.run(context)) return error(add.info());

      // delete old documents if addition was successful
      if(pre != -1) data.delete(pre);
      // flushes changes
      data.flush();
    }
    return info(DOCS_REPLACED_X_X, 1, perf);
  }
}
