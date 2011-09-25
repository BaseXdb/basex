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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Replace extends ACreate {
  /**
   * Default constructor.
   * @param source source path
   * @param input input file or XML string
   */
  public Replace(final String source, final String input) {
    super(DATAREF | User.WRITE, source, input);
  }

  /**
   * Constructor.
   * @param source source path
   */
  public Replace(final String source) {
    super(DATAREF | User.WRITE, source);
  }

  @Override
  protected boolean run() {
    // check if the input source has already been initialized
    if(in == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists()) return error(FILEWHICH, io);
      in = io.inputSource();
    }

    String name = MetaData.normPath(args[0]);
    if(name == null || name.isEmpty()) return error(DIRERR, args[0]);

    final Data data = context.data();
    final int pre = data.doc(name);
    // check if path points to a single file
    if(pre != -1 && data.docs(name).size() != 1) return error(DIRERR, name);

    final IOFile file = data.meta.binary(name);
    if(file != null && file.exists()) {
      // replace binary file if it already exists
      final Store store = new Store(name);
      store.setInput(in);
      if(!store.run(context)) return error(store.info());
    } else {
      // otherwise, add new document as xml
      String trg = "";
      final int i = name.lastIndexOf('/');
      if(i != -1) {
        trg = name.substring(0, i);
        name = name.substring(i + 1);
      }
      final Add add = new Add(null, name, trg);
      add.setInput(in);
      if(!add.run(context)) return error(add.info());

      // delete old documents if addition was successful
      if(pre != -1) data.delete(pre);
      // flushes changes
      data.flush();
    }
    return info(PATHREPLACED, 1, perf);
  }
}
