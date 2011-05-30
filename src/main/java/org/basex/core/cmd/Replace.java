package org.basex.core.cmd;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;

import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;

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

  @Override
  protected boolean run() {
    final Data data = context.data;
    String path = path(args[0]);
    if(path.isEmpty()) return error(DIRERR, path);

    final byte[] src = token(path);
    final int[] docs = data.doc(args[0]);
    // check if path was found
    if(docs.length == 0) return error(FILEWHICH, path);
    // check if path points exclusively to files
    for(final int doc : data.doc(args[0])) {
      if(!eq(data.text(doc, true), src)) return error(DIRERR, path);
    }
    // check if input exists
    final IO io = IO.get(args[1]);
    if(!io.exists()) return error(FILEWHICH, io);

    // delete documents
    for(int d = docs.length - 1; d >= 0; d--) data.delete(docs[d]);

    String target = "";
    final int i = path.lastIndexOf('/');
    if(i != -1) {
      target = path.substring(0, i);
      path = path.substring(i + 1);
    }

    final Add add = new Add(args[1], path, target);
    if(!add.run(context)) {
      context.update();
      data.flush();
      return error(add.info());
    }
    return info(PATHREPLACED, docs.length, perf);
  }
}
