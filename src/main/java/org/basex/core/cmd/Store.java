package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOFile;

/**
 * Evaluates the 'store' command and stores binary content into the database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Store extends ACreate {
  /**
   * Default constructor.
   * @param target target path
   * @param input input file
   */
  public Store(final String target, final String input) {
    super(DATAREF | User.WRITE, target, input);
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    final IOFile bin = data.meta.binary(args[0]);
    if(args[0].isEmpty() || !bin.valid()) return error(NAMEINVALID, args[0]);

    final IO in = IO.get(args[1]);
    if(!in.exists() || in.isDir()) return error(FILEWHICH, in);

    try {
      new IOFile(bin.dir()).md();
      bin.write(in.read());
    } catch(final IOException ex) {
      return error(DBNOTSTORED, in);
    }
    return info(QUERYEXEC, perf);
  }
}
