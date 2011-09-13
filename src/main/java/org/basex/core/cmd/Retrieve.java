package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.IOFile;

/**
 * Evaluates the 'retrieve' command and retrieves binary content.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Retrieve extends ACreate {
  /**
   * Default constructor.
   * @param path path
   */
  public Retrieve(final String path) {
    super(DATAREF, path);
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    final IOFile bin = data.meta.binary(args[0]);
    if(!bin.exists() || bin.isDir()) return error(FILEWHICH, args[0]);

    out.write(bin.read());
    return info(QUERYEXEC, perf);
  }
}
