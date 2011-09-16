package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.list.IntList;

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
    if(is == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists()) return error(FILEWHICH, io);
      is = io.inputSource();
    }

    String name = IOFile.normalize(args[0]);
    if(name.isEmpty()) return error(DIRERR, name);

    final byte[] src = token(name);
    final Data data = context.data();
    final IntList docs = data.docs(name);
    final int ds = docs.size();
    // check if path points exclusively to files
    for(int i = 0; i < ds; i++) {
      if(!eq(data.text(docs.get(i), true), src)) return error(DIRERR, name);
    }

    final IOFile file = data.meta.binary(name);
    try {
      if(file != null && file.exists()) {
        // replace binary file if it already exists
        Store.store(file, is);
      } else {
        // otherwise, add new document as xml
        String trg = "";
        final int i = name.lastIndexOf('/');
        if(i != -1) {
          trg = name.substring(0, i);
          name = name.substring(i + 1);
        }
        final Command add = new Add(null, name, trg).input(is);
        if(!add.run(context)) return error(add.info());

        // delete old documents if addition was successful
        for(int d = ds - 1; d >= 0; d--) data.delete(docs.get(d));
        // flushes changes
        data.flush();
      }
    } catch(final BaseXException ex) {
      return error(ex.getMessage());
    }
    return info(PATHREPLACED, ds);
  }
}
